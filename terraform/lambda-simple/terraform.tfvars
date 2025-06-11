# ========================================
# Configuração JWT Lambda
# ========================================

aws_region = "us-east-1"
environment = "dev"

# Configuração Lambda
lambda_handler = "io.github.bapadua.lambda.handler.JwtValidationHandler::handleRequest"
lambda_runtime = "java21"
lambda_timeout = 30
lambda_memory_size = 512 