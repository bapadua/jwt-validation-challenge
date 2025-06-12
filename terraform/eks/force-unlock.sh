#!/bin/bash

# Script para forçar o unlock do estado do Terraform

# Definições de cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Configurações
DYNAMODB_TABLE="jwt-api-terraform-locks"
LOCK_PATH="jwt-api-terraform-state/eks/terraform.tfstate"

echo -e "${YELLOW}🔓 Script para forçar unlock do estado Terraform${NC}"

# Verifica se estamos logados na AWS
if ! aws sts get-caller-identity &>/dev/null; then
  echo -e "${RED}Erro: Você não está autenticado na AWS. Faça login com 'aws configure' primeiro.${NC}"
  exit 1
fi

echo -e "${YELLOW}Verificando se a tabela DynamoDB existe...${NC}"

# Verifica se a tabela existe
if ! aws dynamodb describe-table --table-name $DYNAMODB_TABLE &>/dev/null; then
  echo -e "${RED}Erro: Tabela $DYNAMODB_TABLE não existe.${NC}"
  echo -e "${YELLOW}Execute o script init-aws.sh primeiro para criar a tabela.${NC}"
  exit 1
fi

echo -e "${GREEN}✅ Tabela $DYNAMODB_TABLE encontrada${NC}"

echo -e "${YELLOW}Verificando locks ativos na tabela DynamoDB...${NC}"

# Lista todos os locks ativos
LOCKS=$(aws dynamodb scan \
  --table-name $DYNAMODB_TABLE \
  --select ALL_ATTRIBUTES \
  --output json 2>/dev/null)

if [ $? -eq 0 ] && [ "$(echo "$LOCKS" | jq '.Count')" -gt 0 ]; then
  echo -e "${YELLOW}Encontrados $(echo "$LOCKS" | jq '.Count') locks ativos:${NC}"
  echo "$LOCKS" | jq -r '.Items[] | "ID: \(.LockID.S), Path: \(.Path.S), Who: \(.Who.S), Created: \(.Created.S)"'
  
  echo -e "\n${YELLOW}Removendo locks...${NC}"
  
  # Remove todos os locks encontrados
  echo "$LOCKS" | jq -r '.Items[].LockID.S' | while read -r lock_id; do
    if [ -n "$lock_id" ]; then
      echo -e "${YELLOW}🔓 Removendo lock: $lock_id${NC}"
      aws dynamodb delete-item \
        --table-name $DYNAMODB_TABLE \
        --key "{\"LockID\":{\"S\":\"$lock_id\"}}" && \
        echo -e "${GREEN}✅ Lock removido: $lock_id${NC}" || \
        echo -e "${RED}❌ Falha ao remover lock: $lock_id${NC}"
    fi
  done
  
  # Verifica se ainda há locks
  sleep 2
  REMAINING=$(aws dynamodb scan \
    --table-name $DYNAMODB_TABLE \
    --select COUNT \
    --query 'Count' \
    --output text 2>/dev/null || echo "0")
  
  if [ "$REMAINING" -eq 0 ]; then
    echo -e "${GREEN}✅ Todos os locks foram removidos com sucesso!${NC}"
  else
    echo -e "${RED}⚠️ Ainda restam $REMAINING locks.${NC}"
  fi
else
  echo -e "${GREEN}✅ Nenhum lock ativo encontrado.${NC}"
fi

echo -e "\n${GREEN}🎉 Estado do Terraform desbloqueado!${NC}"
echo -e "Agora você pode executar o terraform plan/apply novamente." 