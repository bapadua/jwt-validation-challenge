# ========================================
# JWT Project Terraform Variables
# ========================================

# Global Configuration
aws_region     = "us-east-1"
environment    = "production"
project_owner  = "admin@company.com"

# Backend API Configuration
backend_api_instance_count = 2
backend_api_cpu           = 512
backend_api_memory        = 1024

# Database Configuration
db_name             = "jwtdb"
db_username         = "jwtadmin"
db_password         = "ChangeMePlease123!"  # CHANGE THIS!
db_instance_class   = "db.t3.micro"
db_allocated_storage = 20

# Lambda Configuration
lambda_handler     = "io.github.bapadua.lambda.handler.JwtValidationHandler::handleRequest"
lambda_runtime     = "java21"
lambda_timeout     = 30
lambda_memory_size = 512

# Monitoring Configuration
log_retention_days        = 30
enable_detailed_monitoring = false

# Domain Configuration (optional)
domain_name     = ""  # e.g., "api.yourcompany.com"
certificate_arn = ""  # SSL certificate ARN

# CI/CD Configuration
enable_auto_deploy          = true
ecr_lifecycle_policy_count = 10 