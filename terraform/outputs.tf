# ========================================
# Main Terraform Outputs
# JWT Project Infrastructure
# ========================================

# ========================================
# General Information
# ========================================

output "aws_account_id" {
  description = "AWS Account ID"
  value       = data.aws_caller_identity.current.account_id
}

output "aws_region" {
  description = "AWS Region used"
  value       = data.aws_region.current.name
}

output "environment" {
  description = "Environment name"
  value       = var.environment
}

output "project_name" {
  description = "Project name"
  value       = local.project_name
}

# ========================================
# Network Outputs
# ========================================

output "vpc_id" {
  description = "ID of the VPC"
  value       = module.vpc.vpc_id
}

output "private_subnet_ids" {
  description = "IDs of private subnets"
  value       = module.vpc.private_subnet_ids
}

output "public_subnet_ids" {
  description = "IDs of public subnets"
  value       = module.vpc.public_subnet_ids
}

# ========================================
# Backend API Outputs
# ========================================

output "backend_api_url" {
  description = "Backend API Load Balancer URL"
  value       = "http://${module.backend_api.alb_dns_name}"
}

output "backend_api_alb_dns_name" {
  description = "Backend API ALB DNS name"
  value       = module.backend_api.alb_dns_name
}

output "backend_api_alb_zone_id" {
  description = "Backend API ALB Zone ID (for Route53)"
  value       = module.backend_api.alb_zone_id
}

output "backend_api_security_group_id" {
  description = "Security Group ID of the Backend API"
  value       = module.backend_api.security_group_id
}

output "ecs_cluster_name" {
  description = "ECS Cluster name"
  value       = module.backend_api.ecs_cluster_name
}

output "ecs_service_name" {
  description = "ECS Service name"
  value       = module.backend_api.ecs_service_name
}

# ========================================
# Database Outputs
# ========================================

output "database_endpoint" {
  description = "RDS instance endpoint"
  value       = module.rds.db_endpoint
  sensitive   = true
}

output "database_port" {
  description = "RDS instance port"
  value       = module.rds.db_port
}

output "database_name" {
  description = "Database name"
  value       = var.db_name
}

# ========================================
# Lambda Outputs
# ========================================

output "lambda_staging_arn" {
  description = "ARN of Lambda staging function"
  value       = module.lambda.staging_function_arn
}

output "lambda_staging_name" {
  description = "Name of Lambda staging function"
  value       = module.lambda.staging_function_name
}

output "lambda_production_arn" {
  description = "ARN of Lambda production function"
  value       = module.lambda.production_function_arn
}

output "lambda_production_name" {
  description = "Name of Lambda production function"
  value       = module.lambda.production_function_name
}

# ========================================
# Container Registry Outputs
# ========================================

output "ecr_repository_url" {
  description = "ECR repository URL for backend API"
  value       = module.ecr.repository_url
}

output "ecr_repository_arn" {
  description = "ECR repository ARN"
  value       = module.ecr.repository_arn
}

# ========================================
# Storage Outputs
# ========================================

output "lambda_artifacts_bucket" {
  description = "S3 bucket for Lambda artifacts"
  value       = module.s3.lambda_artifacts_bucket_name
}

output "backend_api_assets_bucket" {
  description = "S3 bucket for backend API assets (optional)"
  value       = module.s3.backend_api_assets_bucket_name
}

# ========================================
# IAM Outputs
# ========================================

output "lambda_execution_role_arn" {
  description = "Lambda execution role ARN"
  value       = module.iam.lambda_execution_role_arn
}

output "ecs_task_role_arn" {
  description = "ECS task role ARN"
  value       = module.iam.ecs_task_role_arn
}

output "ecs_execution_role_arn" {
  description = "ECS execution role ARN"
  value       = module.iam.ecs_execution_role_arn
}

# ========================================
# GitHub Actions Secrets
# ========================================

output "github_actions_secrets" {
  description = "Values to configure in GitHub Actions secrets"
  value = {
    # AWS Configuration
    AWS_REGION = data.aws_region.current.name
    
    # Backend API
    ECR_REPOSITORY_URI           = module.ecr.repository_url
    ECS_CLUSTER_NAME            = module.backend_api.ecs_cluster_name
    ECS_SERVICE_NAME            = module.backend_api.ecs_service_name
    BACKEND_API_URL             = "http://${module.backend_api.alb_dns_name}"
    
    # Lambda
    LAMBDA_FUNCTION_NAME_STAGING = module.lambda.staging_function_name
    LAMBDA_FUNCTION_NAME_PROD    = module.lambda.production_function_name
    LAMBDA_DEPLOYMENT_BUCKET     = module.s3.lambda_artifacts_bucket_name
    
    # Database (for application configuration)
    DATABASE_ENDPOINT = module.rds.db_endpoint
    DATABASE_NAME     = var.db_name
    DATABASE_USERNAME = var.db_username
  }
  sensitive = false
}

# ========================================
# Application Configuration
# ========================================

output "application_config" {
  description = "Configuration values for the applications"
  value = {
    backend_api = {
      database_url = "jdbc:postgresql://${module.rds.db_endpoint}:5432/${var.db_name}"
      app_url      = "http://${module.backend_api.alb_dns_name}"
      health_check = "http://${module.backend_api.alb_dns_name}/actuator/health"
    }
    lambda = {
      staging_name    = module.lambda.staging_function_name
      production_name = module.lambda.production_function_name
      backend_api_url = "http://${module.backend_api.alb_dns_name}"
    }
  }
  sensitive = false
}

# ========================================
# Monitoring URLs
# ========================================

output "monitoring_urls" {
  description = "URLs for monitoring and observability"
  value = {
    cloudwatch_logs = "https://console.aws.amazon.com/cloudwatch/home?region=${data.aws_region.current.name}#logsV2:log-groups"
    cloudwatch_metrics = "https://console.aws.amazon.com/cloudwatch/home?region=${data.aws_region.current.name}#metricsV2:graph=~();search=${local.project_name}"
    ecs_console = "https://console.aws.amazon.com/ecs/home?region=${data.aws_region.current.name}#/clusters/${module.backend_api.ecs_cluster_name}/services"
    lambda_console = "https://console.aws.amazon.com/lambda/home?region=${data.aws_region.current.name}#/functions"
    rds_console = "https://console.aws.amazon.com/rds/home?region=${data.aws_region.current.name}#databases:"
  }
}

# ========================================
# Cost Estimation
# ========================================

output "estimated_monthly_cost" {
  description = "Estimated monthly cost breakdown (USD)"
  value = {
    backend_api = {
      ecs_fargate = "~$30-50 (depending on usage)"
      alb         = "~$22.50"
      nat_gateway = "~$45"
    }
    database = {
      rds_instance = "~$15-25 (db.t3.micro)"
      storage      = "~$2-4 (20GB)"
    }
    lambda = {
      execution = "~$0-5 (first 1M requests free)"
    }
    storage = {
      s3 = "~$1-3"
      ecr = "~$1-2"
    }
    total_estimated = "~$120-150/month"
    note = "Costs may vary based on actual usage and AWS pricing changes"
  }
} 