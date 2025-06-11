# ========================================
# Variáveis Básicas para JWT Lambda
# ========================================

variable "aws_region" {
  description = "Região AWS"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Ambiente (dev, staging, production)"
  type        = string
  default     = "dev"
}

variable "lambda_handler" {
  description = "Handler do Lambda"
  type        = string
  default     = "Handler::handleRequest"
}

variable "lambda_runtime" {
  description = "Runtime do Lambda"
  type        = string
  default     = "java21"
}

variable "lambda_timeout" {
  description = "Timeout em segundos"
  type        = number
  default     = 30
}

variable "lambda_memory_size" {
  description = "Memória em MB"
  type        = number
  default     = 512
} 