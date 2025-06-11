# ========================================
# Global Variables
# JWT Project Infrastructure
# ========================================

variable "aws_region" {
  description = "AWS region for all resources"
  type        = string
  default     = "us-east-1"
  
  validation {
    condition = can(regex("^[a-z0-9-]+$", var.aws_region))
    error_message = "AWS region must be a valid region identifier."
  }
}

variable "environment" {
  description = "Environment name (e.g., dev, staging, production)"
  type        = string
  default     = "dev"
  
  validation {
    condition = contains(["dev", "staging", "production"], var.environment)
    error_message = "Environment must be one of: dev, staging, production."
  }
}

variable "project_owner" {
  description = "Email of the project owner"
  type        = string
  default     = "admin@company.com"
}

# ========================================
# Backend API Variables
# ========================================

variable "backend_api_instance_count" {
  description = "Number of ECS tasks for the backend API"
  type        = number
  default     = 2
  
  validation {
    condition = var.backend_api_instance_count >= 1 && var.backend_api_instance_count <= 10
    error_message = "Backend API instance count must be between 1 and 10."
  }
}

variable "backend_api_cpu" {
  description = "CPU units for backend API ECS task (1024 = 1 vCPU)"
  type        = number
  default     = 512
  
  validation {
    condition = contains([256, 512, 1024, 2048, 4096], var.backend_api_cpu)
    error_message = "CPU must be one of: 256, 512, 1024, 2048, 4096."
  }
}

variable "backend_api_memory" {
  description = "Memory (MB) for backend API ECS task"
  type        = number
  default     = 1024
  
  validation {
    condition = var.backend_api_memory >= 512 && var.backend_api_memory <= 8192
    error_message = "Memory must be between 512 and 8192 MB."
  }
}

# ========================================
# Database Variables
# ========================================

variable "db_name" {
  description = "Name of the PostgreSQL database"
  type        = string
  default     = "jwtdb"
  
  validation {
    condition = can(regex("^[a-zA-Z][a-zA-Z0-9_]*$", var.db_name))
    error_message = "Database name must start with a letter and contain only alphanumeric characters and underscores."
  }
}

variable "db_username" {
  description = "Master username for the database"
  type        = string
  default     = "jwtadmin"
  sensitive   = true
}

variable "db_password" {
  description = "Master password for the database"
  type        = string
  sensitive   = true
  
  validation {
    condition = length(var.db_password) >= 8
    error_message = "Database password must be at least 8 characters long."
  }
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
  
  validation {
    condition = can(regex("^db\\.", var.db_instance_class))
    error_message = "Database instance class must start with 'db.'."
  }
}

variable "db_allocated_storage" {
  description = "Allocated storage for RDS instance (GB)"
  type        = number
  default     = 20
  
  validation {
    condition = var.db_allocated_storage >= 20 && var.db_allocated_storage <= 1000
    error_message = "Allocated storage must be between 20 and 1000 GB."
  }
}

# ========================================
# Lambda Variables
# ========================================

variable "lambda_handler" {
  description = "Lambda function handler"
  type        = string
  default     = "io.github.bapadua.lambda.handler.JwtValidationHandler::handleRequest"
}

variable "lambda_runtime" {
  description = "Lambda runtime"
  type        = string
  default     = "java21"
  
  validation {
    condition = contains(["java8.al2", "java11", "java17", "java21"], var.lambda_runtime)
    error_message = "Lambda runtime must be a valid Java runtime."
  }
}

variable "lambda_timeout" {
  description = "Lambda function timeout (seconds)"
  type        = number
  default     = 30
  
  validation {
    condition = var.lambda_timeout >= 1 && var.lambda_timeout <= 900
    error_message = "Lambda timeout must be between 1 and 900 seconds."
  }
}

variable "lambda_memory_size" {
  description = "Lambda function memory size (MB)"
  type        = number
  default     = 512
  
  validation {
    condition = var.lambda_memory_size >= 128 && var.lambda_memory_size <= 10240
    error_message = "Lambda memory size must be between 128 and 10240 MB."
  }
}

# ========================================
# Monitoring Variables
# ========================================

variable "log_retention_days" {
  description = "CloudWatch log retention period (days)"
  type        = number
  default     = 14
  
  validation {
    condition = contains([1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1827, 3653], var.log_retention_days)
    error_message = "Log retention days must be a valid CloudWatch retention period."
  }
}

variable "enable_detailed_monitoring" {
  description = "Enable detailed CloudWatch monitoring"
  type        = bool
  default     = false
}

# ========================================
# Domain Variables (Optional)
# ========================================

variable "domain_name" {
  description = "Custom domain name for the backend API (optional)"
  type        = string
  default     = ""
}

variable "certificate_arn" {
  description = "SSL certificate ARN for HTTPS (optional)"
  type        = string
  default     = ""
}

# ========================================
# CI/CD Variables
# ========================================

variable "enable_auto_deploy" {
  description = "Enable automatic deployment on image push"
  type        = bool
  default     = true
}

variable "ecr_lifecycle_policy_count" {
  description = "Number of images to keep in ECR"
  type        = number
  default     = 10
  
  validation {
    condition = var.ecr_lifecycle_policy_count >= 5 && var.ecr_lifecycle_policy_count <= 50
    error_message = "ECR lifecycle policy count must be between 5 and 50."
  }
} 