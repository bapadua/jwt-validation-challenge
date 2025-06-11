# ========================================
# JWT Project Infrastructure
# Backend Challenge + AWS Lambda
# ========================================

terraform {
  required_version = ">= 1.5"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.0"
    }
  }
  
  # Backend S3 para state (configure depois)
  # backend "s3" {
  #   bucket         = "jwt-terraform-state-bucket"
  #   key            = "jwt-infrastructure/terraform.tfstate"
  #   region         = "us-east-1"
  #   encrypt        = true
  #   dynamodb_table = "terraform-locks"
  # }
}

# ========================================
# Provider Configuration
# ========================================

provider "aws" {
  region = var.aws_region
  
  default_tags {
    tags = local.common_tags
  }
}

# ========================================
# Local Values
# ========================================

locals {
  project_name = "jwt-project"
  environment  = var.environment
  
  common_tags = {
    Project     = local.project_name
    Environment = local.environment
    ManagedBy   = "terraform"
    Repository  = "josewebtoken"
    Owner       = var.project_owner
    CreatedBy   = "terraform"
  }
  
  # Naming conventions
  backend_api_name       = "${local.project_name}-backend-api"
  lambda_staging_name    = "${local.project_name}-lambda-staging"
  lambda_production_name = "${local.project_name}-lambda-production"
  
  # Network configuration
  vpc_cidr = "10.0.0.0/16"
  azs      = slice(data.aws_availability_zones.available.names, 0, 2)
}

# ========================================
# Data Sources
# ========================================

data "aws_caller_identity" "current" {}
data "aws_region" "current" {}
data "aws_availability_zones" "available" {
  state = "available"
}

# ========================================
# Random IDs for unique naming
# ========================================

resource "random_id" "suffix" {
  byte_length = 4
}

# ========================================
# VPC Module (Shared Network)
# ========================================

module "vpc" {
  source = "./modules/vpc"
  
  project_name = local.project_name
  environment  = local.environment
  common_tags  = local.common_tags
  
  vpc_cidr = local.vpc_cidr
  azs      = local.azs
  
  # Subnets
  private_subnets = [
    "10.0.1.0/24",
    "10.0.2.0/24"
  ]
  public_subnets = [
    "10.0.101.0/24",
    "10.0.102.0/24"
  ]
  
  enable_nat_gateway = true
  enable_vpn_gateway = false
  enable_dns_hostnames = true
  enable_dns_support = true
}

# ========================================
# IAM Module (Shared IAM Resources)
# ========================================

module "iam" {
  source = "./modules/iam"
  
  project_name = local.project_name
  environment  = local.environment
  common_tags  = local.common_tags
  
  # Backend API
  backend_api_name = local.backend_api_name
  
  # Lambda
  lambda_function_names = [
    local.lambda_staging_name,
    local.lambda_production_name
  ]
}

# ========================================
# S3 Module (Shared Storage)
# ========================================

module "s3" {
  source = "./modules/s3"
  
  project_name = local.project_name
  environment  = local.environment
  common_tags  = local.common_tags
  
  random_suffix = random_id.suffix.hex
}

# ========================================
# ECR Module (Container Registry)
# ========================================

module "ecr" {
  source = "./modules/ecr"
  
  project_name = local.project_name
  environment  = local.environment
  common_tags  = local.common_tags
  
  repository_name = local.backend_api_name
}

# ========================================
# RDS Module (Database for Backend API)
# ========================================

module "rds" {
  source = "./modules/rds"
  
  project_name = local.project_name
  environment  = local.environment
  common_tags  = local.common_tags
  
  # Network
  vpc_id              = module.vpc.vpc_id
  private_subnet_ids  = module.vpc.private_subnet_ids
  
  # Database configuration
  db_name     = var.db_name
  db_username = var.db_username
  db_password = var.db_password
  
  # Instance configuration
  instance_class    = var.db_instance_class
  allocated_storage = var.db_allocated_storage
  
  # Security
  allowed_security_group_ids = [module.backend_api.security_group_id]
}

# ========================================
# Backend API Module (ECS + ALB)
# ========================================

module "backend_api" {
  source = "./modules/backend-api"
  
  project_name = local.project_name
  environment  = local.environment
  common_tags  = local.common_tags
  
  # Network
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  public_subnet_ids  = module.vpc.public_subnet_ids
  
  # ECS Configuration
  app_name        = local.backend_api_name
  app_port        = 8080
  app_count       = var.backend_api_instance_count
  
  # Container Configuration
  ecr_repository_url = module.ecr.repository_url
  container_image    = "${module.ecr.repository_url}:latest"
  
  # IAM
  ecs_task_role_arn      = module.iam.ecs_task_role_arn
  ecs_execution_role_arn = module.iam.ecs_execution_role_arn
  
  # Environment Variables
  environment_variables = [
    {
      name  = "SPRING_PROFILES_ACTIVE"
      value = "aws,${local.environment}"
    },
    {
      name  = "SPRING_DATASOURCE_URL"
      value = "jdbc:postgresql://${module.rds.db_endpoint}:5432/${var.db_name}"
    },
    {
      name  = "SPRING_DATASOURCE_USERNAME"
      value = var.db_username
    },
    {
      name  = "LOG_LEVEL"
      value = var.environment == "production" ? "INFO" : "DEBUG"
    }
  ]
  
  # Secrets (sensitive data)
  secrets = [
    {
      name      = "SPRING_DATASOURCE_PASSWORD"
      valueFrom = module.rds.db_password_secret_arn
    }
  ]
  
  # Health check
  health_check_path = "/actuator/health"
  
  depends_on = [
    module.vpc,
    module.iam,
    module.rds,
    module.ecr
  ]
}

# ========================================
# Lambda Module
# ========================================

module "lambda" {
  source = "./modules/lambda"
  
  project_name = local.project_name
  environment  = local.environment
  common_tags  = local.common_tags
  
  # IAM
  lambda_execution_role_arn = module.iam.lambda_execution_role_arn
  
  # S3
  deployment_bucket = module.s3.lambda_artifacts_bucket_name
  
  # Lambda Configuration
  lambda_staging_name    = local.lambda_staging_name
  lambda_production_name = local.lambda_production_name
  
  lambda_handler     = var.lambda_handler
  lambda_runtime     = var.lambda_runtime
  lambda_timeout     = var.lambda_timeout
  lambda_memory_size = var.lambda_memory_size
  
  # Environment variables
  staging_env_vars = {
    ENVIRONMENT = "staging"
    LOG_LEVEL   = "DEBUG"
    PROJECT     = local.project_name
    BACKEND_API_URL = "https://${module.backend_api.alb_dns_name}"
  }
  
  production_env_vars = {
    ENVIRONMENT = "production"
    LOG_LEVEL   = "INFO"
    PROJECT     = local.project_name
    BACKEND_API_URL = "https://${module.backend_api.alb_dns_name}"
  }
  
  depends_on = [
    module.iam,
    module.s3
  ]
}

# ========================================
# CloudWatch Module (Monitoring)
# ========================================

module "cloudwatch" {
  source = "./modules/cloudwatch"
  
  project_name = local.project_name
  environment  = local.environment
  common_tags  = local.common_tags
  
  # Lambda monitoring
  lambda_function_names = [
    local.lambda_staging_name,
    local.lambda_production_name
  ]
  
  # Backend API monitoring
  backend_api_name = local.backend_api_name
  alb_arn_suffix   = module.backend_api.alb_arn_suffix
  target_group_arn_suffix = module.backend_api.target_group_arn_suffix
  
  # Log retention
  log_retention_days = var.log_retention_days
  
  depends_on = [
    module.lambda,
    module.backend_api
  ]
} 