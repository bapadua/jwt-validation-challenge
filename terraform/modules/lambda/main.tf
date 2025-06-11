# ========================================
# Lambda Module
# Staging and Production Functions
# ========================================

# CloudWatch Log Groups
resource "aws_cloudwatch_log_group" "lambda_staging" {
  name              = "/aws/lambda/${var.lambda_staging_name}"
  retention_in_days = 14

  tags = merge(var.common_tags, {
    Name = "${var.lambda_staging_name}-logs"
  })
}

resource "aws_cloudwatch_log_group" "lambda_production" {
  name              = "/aws/lambda/${var.lambda_production_name}"
  retention_in_days = 30

  tags = merge(var.common_tags, {
    Name = "${var.lambda_production_name}-logs"
  })
}

# Lambda Function - Staging
resource "aws_lambda_function" "staging" {
  function_name = var.lambda_staging_name
  role         = var.lambda_execution_role_arn
  handler      = var.lambda_handler
  runtime      = var.lambda_runtime
  timeout      = var.lambda_timeout
  memory_size  = var.lambda_memory_size

  # Placeholder code (will be updated by CI/CD)
  filename         = data.archive_file.lambda_zip.output_path
  source_code_hash = data.archive_file.lambda_zip.output_base64sha256

  environment {
    variables = var.staging_env_vars
  }

  depends_on = [aws_cloudwatch_log_group.lambda_staging]

  tags = merge(var.common_tags, {
    Name = var.lambda_staging_name
    Stage = "staging"
  })
}

# Lambda Function - Production
resource "aws_lambda_function" "production" {
  function_name = var.lambda_production_name
  role         = var.lambda_execution_role_arn
  handler      = var.lambda_handler
  runtime      = var.lambda_runtime
  timeout      = var.lambda_timeout
  memory_size  = var.lambda_memory_size

  # Placeholder code (will be updated by CI/CD)
  filename         = data.archive_file.lambda_zip.output_path
  source_code_hash = data.archive_file.lambda_zip.output_base64sha256

  environment {
    variables = var.production_env_vars
  }

  depends_on = [aws_cloudwatch_log_group.lambda_production]

  tags = merge(var.common_tags, {
    Name = var.lambda_production_name
    Stage = "production"
  })
}

# Lambda Function URL - Staging (optional)
resource "aws_lambda_function_url" "staging" {
  count = var.enable_function_url ? 1 : 0
  
  function_name      = aws_lambda_function.staging.function_name
  authorization_type = "NONE"

  cors {
    allow_credentials = false
    allow_origins     = ["*"]
    allow_methods     = ["*"]
    allow_headers     = ["date", "keep-alive"]
    expose_headers    = ["date", "keep-alive"]
    max_age          = 86400
  }
}

# Lambda Function URL - Production (optional)
resource "aws_lambda_function_url" "production" {
  count = var.enable_function_url ? 1 : 0
  
  function_name      = aws_lambda_function.production.function_name
  authorization_type = "NONE"

  cors {
    allow_credentials = false
    allow_origins     = ["*"]
    allow_methods     = ["*"]
    allow_headers     = ["date", "keep-alive"]
    expose_headers    = ["date", "keep-alive"]
    max_age          = 86400
  }
}

# Lambda Alias - Staging
resource "aws_lambda_alias" "staging" {
  name             = "staging"
  description      = "Staging alias for ${var.lambda_staging_name}"
  function_name    = aws_lambda_function.staging.function_name
  function_version = "$LATEST"
}

# Lambda Alias - Production
resource "aws_lambda_alias" "production" {
  name             = "production"
  description      = "Production alias for ${var.lambda_production_name}"
  function_name    = aws_lambda_function.production.function_name
  function_version = "$LATEST"
}

# Placeholder ZIP file for initial deployment
data "archive_file" "lambda_zip" {
  type        = "zip"
  output_path = "/tmp/lambda-placeholder.zip"
  
  source {
    content = <<EOF
public class PlaceholderHandler {
    public String handleRequest(Object input, Context context) {
        return "Placeholder - waiting for deployment";
    }
}
EOF
    filename = "PlaceholderHandler.java"
  }
} 