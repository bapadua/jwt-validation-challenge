#!/bin/bash

# Script para forçar o unlock do estado do Terraform

# Definições de cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Configurações
LOCK_ID="ec73e656-8851-3fbf-7576-4cd92d1d28a3"
DYNAMODB_TABLE="jwt-api-terraform-locks"

echo -e "${YELLOW}🔓 Script para forçar unlock do estado Terraform${NC}"

# Verifica se estamos logados na AWS
if ! aws sts get-caller-identity &>/dev/null; then
  echo -e "${RED}Erro: Você não está autenticado na AWS. Faça login com 'aws configure' primeiro.${NC}"
  exit 1
fi

echo -e "${YELLOW}Verificando locks ativos na tabela DynamoDB...${NC}"

# Lista todos os locks ativos
aws dynamodb scan \
  --table-name $DYNAMODB_TABLE \
  --select ALL_ATTRIBUTES \
  --output table

echo -e "\n${YELLOW}Removendo lock específico: ${LOCK_ID}${NC}"

# Remove o lock específico
aws dynamodb delete-item \
  --table-name $DYNAMODB_TABLE \
  --key "{\"LockID\":{\"S\":\"jwt-api-terraform-state/eks/terraform.tfstate\"}}" \
  --output table

echo -e "${GREEN}✅ Lock removido com sucesso!${NC}"

echo -e "\n${YELLOW}Verificando se ainda há locks ativos...${NC}"
aws dynamodb scan \
  --table-name $DYNAMODB_TABLE \
  --select ALL_ATTRIBUTES \
  --output table

echo -e "\n${GREEN}🎉 Estado do Terraform desbloqueado!${NC}"
echo -e "Agora você pode executar o terraform plan/apply novamente." 