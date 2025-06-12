# ========================================
# JWT Lambda - Estrutura Mínima
# ========================================

terraform {
  required_version = ">= 1.5"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    archive = {
      source  = "hashicorp/archive"
      version = "~> 2.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# ========================================
# IAM Role existente (não criar nova)
# ========================================

data "aws_iam_role" "existing_lambda_role" {
  name = "jwt-lambda-role"
}

# ========================================
# Código do Lambda (JAR real)
# ========================================

data "archive_file" "lambda_zip" {
  type        = "zip"
  output_path = "/tmp/lambda.zip"
  source_file = "../../aws-lambda-jwt/target/aws-lambda-jwt-0.0.1-SNAPSHOT.jar"
}

# ========================================
# Lambda Functions
# ========================================

resource "aws_lambda_function" "jwt_lambda" {
  function_name = "jwt-validator-v2"
  role         = data.aws_iam_role.existing_lambda_role.arn
  handler      = var.lambda_handler
  runtime      = var.lambda_runtime
  timeout      = var.lambda_timeout
  memory_size  = var.lambda_memory_size

  filename         = data.archive_file.lambda_zip.output_path
  source_code_hash = data.archive_file.lambda_zip.output_base64sha256

  environment {
    variables = {
      ENVIRONMENT = var.environment
    }
  }

  tags = {
    Name        = "jwt-validator-v2"
    Environment = var.environment
    Project     = "jwt-validator"
  }
}

# ========================================
# Function URL (acesso HTTP direto)
# ========================================

resource "aws_lambda_function_url" "jwt_lambda_url" {
  function_name      = aws_lambda_function.jwt_lambda.function_name
  authorization_type = "NONE"

  cors {
    allow_credentials = false
    allow_origins     = ["*"]
    allow_methods     = ["GET", "POST"]
    allow_headers     = ["content-type"]
    max_age          = 86400
  }
} 