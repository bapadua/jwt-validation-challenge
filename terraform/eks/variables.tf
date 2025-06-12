# ========================================
# Variables for JWT API EKS infrastructure
# ========================================

variable "aws_region" {
  description = "Região AWS onde os recursos serão implantados"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Ambiente de implantação (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "cluster_name" {
  description = "Nome do cluster EKS"
  type        = string
  default     = "jwt-api-cluster"
}

variable "grafana_admin_password" {
  description = "Senha de administrador para o Grafana"
  type        = string
  sensitive   = true
  default     = "AdminPassword123" # Altere para um valor mais seguro ou use variável de ambiente
} 