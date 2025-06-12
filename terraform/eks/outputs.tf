# ========================================
# Outputs
# ========================================

output "vpc_id" {
  description = "ID da VPC criada"
  value       = module.vpc.vpc_id
}

output "private_subnets" {
  description = "IDs das subnets privadas"
  value       = module.vpc.private_subnets
}

output "public_subnets" {
  description = "IDs das subnets públicas"
  value       = module.vpc.public_subnets
}

output "cluster_name" {
  description = "Nome do cluster EKS"
  value       = module.eks.cluster_name
}

output "cluster_endpoint" {
  description = "Endpoint do cluster EKS"
  value       = module.eks.cluster_endpoint
}

output "cluster_certificate_authority_data" {
  description = "Certificado de autoridade do cluster EKS"
  value       = module.eks.cluster_certificate_authority_data
  sensitive   = true
}

output "aws_load_balancer_controller_iam_role_arn" {
  description = "ARN do IAM Role para o AWS Load Balancer Controller"
  value       = module.load_balancer_controller.iam_role_arn
}

output "jwt_api_url" {
  description = "URL para acessar a API JWT"
  value       = "http://api.jwt-demo.com"
}

output "grafana_url" {
  description = "URL para acessar o Grafana"
  value       = "https://monitoring.jwt-demo.com"
}

output "grafana_admin_user" {
  description = "Usuário admin do Grafana"
  value       = "admin"
}

# O valor real será extraído das variáveis do Terraform
output "grafana_admin_password" {
  description = "Senha do usuário admin do Grafana (sensível)"
  value       = var.grafana_admin_password
  sensitive   = true
} 