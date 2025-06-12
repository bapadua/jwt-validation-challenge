# ========================================
# JWT API - EKS Infrastructure
# ========================================

terraform {
  required_version = ">= 1.5"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.11"
    }
    kubectl = {
      source  = "gavinbunney/kubectl"
      version = "~> 1.14"
    }
  }
  
  backend "s3" {
    bucket         = "jwt-api-terraform-state"
    key            = "eks/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "jwt-api-terraform-locks"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region
}

# ========================================
# Data Sources
# ========================================

data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_caller_identity" "current" {}

# ========================================
# Network Infrastructure
# ========================================

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "jwt-api-vpc"
  cidr = "10.0.0.0/16"

  azs             = slice(data.aws_availability_zones.available.names, 0, 2)
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]

  enable_nat_gateway     = true
  single_nat_gateway     = true
  one_nat_gateway_per_az = false

  enable_dns_hostnames = true
  enable_dns_support   = true

  # Tags required for EKS
  public_subnet_tags = {
    "kubernetes.io/cluster/${var.cluster_name}" = "shared"
    "kubernetes.io/role/elb"                    = "1"
  }

  private_subnet_tags = {
    "kubernetes.io/cluster/${var.cluster_name}" = "shared"
    "kubernetes.io/role/internal-elb"           = "1"
  }

  tags = {
    Project     = "jwt-api"
    Environment = var.environment
  }
}

# ========================================
# EKS Cluster
# ========================================

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 19.15"

  cluster_name    = var.cluster_name
  cluster_version = "1.27"

  cluster_endpoint_public_access  = true
  cluster_endpoint_private_access = true

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  # Self managed node group defaults
  self_managed_node_group_defaults = {
    instance_type                          = "t3.medium"
    update_launch_template_default_version = true
  }

  # EKS Managed Node Group(s)
  eks_managed_node_group_defaults = {
    ami_type       = "AL2_x86_64"
    instance_types = ["t3.medium"]
    
    attach_cluster_primary_security_group = true
  }

  eks_managed_node_groups = {
    main = {
      name = "jwt-api-node-group"

      min_size     = 1
      max_size     = 3
      desired_size = 2

      capacity_type  = "ON_DEMAND"
      instance_types = ["t3.medium"]

      labels = {
        role = "general"
      }
      
      tags = {
        Project     = "jwt-api"
        Environment = var.environment
      }
    }
  }

  # aws-auth configmap
  create_aws_auth_configmap = false
  manage_aws_auth_configmap = true

  aws_auth_roles = [
    {
      rolearn  = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/Admin"
      username = "admin"
      groups   = ["system:masters"]
    },
  ]

  tags = {
    Project     = "jwt-api"
    Environment = var.environment
  }
}

# ========================================
# Kubernetes & Helm Providers 
# ========================================

data "aws_eks_cluster" "this" {
  name = module.eks.cluster_name
  depends_on = [module.eks.eks_managed_node_groups]
}

data "aws_eks_cluster_auth" "this" {
  name = module.eks.cluster_name
  depends_on = [module.eks.eks_managed_node_groups]
}

provider "kubernetes" {
  host                   = data.aws_eks_cluster.this.endpoint
  cluster_ca_certificate = base64decode(data.aws_eks_cluster.this.certificate_authority[0].data)
  token                  = data.aws_eks_cluster_auth.this.token
}

provider "helm" {
  kubernetes {
    host                   = data.aws_eks_cluster.this.endpoint
    cluster_ca_certificate = base64decode(data.aws_eks_cluster.this.certificate_authority[0].data)
    token                  = data.aws_eks_cluster_auth.this.token
  }
}

provider "kubectl" {
  host                   = data.aws_eks_cluster.this.endpoint
  cluster_ca_certificate = base64decode(data.aws_eks_cluster.this.certificate_authority[0].data)
  token                  = data.aws_eks_cluster_auth.this.token
  load_config_file       = false
}

# ========================================
# AWS Load Balancer Controller
# ========================================

module "load_balancer_controller" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.20"

  role_name                              = "eks-${var.cluster_name}-aws-load-balancer-controller"
  attach_load_balancer_controller_policy = true

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:aws-load-balancer-controller"]
    }
  }

  tags = {
    Project     = "jwt-api"
    Environment = var.environment
  }
}

resource "kubernetes_service_account" "aws_load_balancer_controller" {
  metadata {
    name      = "aws-load-balancer-controller"
    namespace = "kube-system"
    annotations = {
      "eks.amazonaws.com/role-arn" = module.load_balancer_controller.iam_role_arn
    }
    labels = {
      "app.kubernetes.io/component" = "controller"
      "app.kubernetes.io/name"      = "aws-load-balancer-controller"
    }
  }
  depends_on = [module.eks.eks_managed_node_groups]
}

resource "helm_release" "aws_load_balancer_controller" {
  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"
  version    = "1.6.2"

  set {
    name  = "clusterName"
    value = var.cluster_name
  }

  set {
    name  = "serviceAccount.create"
    value = "false"
  }

  set {
    name  = "serviceAccount.name"
    value = kubernetes_service_account.aws_load_balancer_controller.metadata[0].name
  }

  depends_on = [
    kubernetes_service_account.aws_load_balancer_controller,
    module.load_balancer_controller
  ]
}

# ========================================
# Monitoring Infrastructure
# ========================================

resource "kubernetes_namespace" "monitoring" {
  metadata {
    name = "monitoring"
  }
  depends_on = [module.eks.eks_managed_node_groups]
}

resource "helm_release" "prometheus" {
  name       = "prometheus"
  repository = "https://prometheus-community.github.io/helm-charts"
  chart      = "kube-prometheus-stack"
  namespace  = kubernetes_namespace.monitoring.metadata[0].name
  version    = "45.7.1"
  
  values = [
    templatefile("${path.module}/values/kube-prometheus-stack-values.yaml", {
      grafana_admin_password = var.grafana_admin_password
    })
  ]
  
  depends_on = [kubernetes_namespace.monitoring]
}

# ========================================
# JWT API Deployment
# ========================================

resource "kubernetes_namespace" "jwt_api" {
  metadata {
    name = "jwt-api"
    labels = {
      name = "jwt-api"
      environment = var.environment
    }
  }
  depends_on = [module.eks.eks_managed_node_groups]
}

resource "helm_release" "jwt_api" {
  name       = "jwt-api"
  chart      = "${path.module}/../../backend-challenge/helm/jwt-validation-api"
  namespace  = kubernetes_namespace.jwt_api.metadata[0].name
  version    = "0.1.0"
  
  # Configurações específicas para a aplicação
  set {
    name  = "replicaCount"
    value = 2
  }

  set {
    name  = "ingress.enabled"
    value = "true"
  }

  set {
    name  = "ingress.annotations.kubernetes\\.io/ingress\\.class"
    value = "alb"
  }
  
  set {
    name  = "ingress.annotations.alb\\.ingress\\.kubernetes\\.io/scheme"
    value = "internet-facing"
  }
  
  set {
    name  = "ingress.annotations.alb\\.ingress\\.kubernetes\\.io/target-type"
    value = "ip"
  }
  
  set {
    name  = "ingress.hosts[0].host"
    value = "api.jwt-demo.com"
  }
  
  set {
    name  = "ingress.hosts[0].paths[0].path"
    value = "/"
  }
  
  set {
    name  = "ingress.hosts[0].paths[0].pathType"
    value = "Prefix"
  }
  
  set {
    name  = "monitoring.enabled"
    value = "true"
  }
  
  set {
    name  = "monitoring.serviceMonitor.enabled"
    value = "true"
  }
  
  depends_on = [
    kubernetes_namespace.jwt_api,
    helm_release.prometheus
  ]
}

# Configuração do Ingress para Grafana
resource "kubectl_manifest" "grafana_ingress" {
  yaml_body = <<-YAML
  apiVersion: networking.k8s.io/v1
  kind: Ingress
  metadata:
    name: grafana-ingress
    namespace: monitoring
    annotations:
      kubernetes.io/ingress.class: alb
      alb.ingress.kubernetes.io/scheme: internet-facing
      alb.ingress.kubernetes.io/target-type: ip
      alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS":443}]'
  spec:
    rules:
      - host: monitoring.jwt-demo.com
        http:
          paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: prometheus-grafana
                port:
                  number: 80
  YAML
  
  depends_on = [
    helm_release.prometheus,
    helm_release.aws_load_balancer_controller
  ]
} 