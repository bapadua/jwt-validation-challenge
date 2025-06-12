#!/bin/bash

# Script para inicializar recursos AWS necessários para o Terraform State remoto

# Definições de cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Região padrão AWS
AWS_REGION=${AWS_REGION:-"us-east-1"}
BUCKET_NAME="jwt-api-terraform-state"
DYNAMODB_TABLE="jwt-api-terraform-locks"

echo -e "${YELLOW}Inicializando recursos AWS para o Backend Terraform...${NC}"
echo -e "Região AWS: ${GREEN}${AWS_REGION}${NC}"

# Verifica se estamos logados na AWS
if ! aws sts get-caller-identity &>/dev/null; then
  echo -e "${RED}Erro: Você não está autenticado na AWS. Faça login com 'aws configure' primeiro.${NC}"
  exit 1
fi

# Cria o bucket S3 se não existir
if aws s3api head-bucket --bucket ${BUCKET_NAME} 2>/dev/null; then
  echo -e "Bucket do Terraform state ${GREEN}${BUCKET_NAME}${NC} já existe"
else
  echo -e "Criando bucket S3 para o Terraform state: ${GREEN}${BUCKET_NAME}${NC}"
  aws s3api create-bucket --bucket ${BUCKET_NAME} --region ${AWS_REGION} \
    --create-bucket-configuration LocationConstraint=${AWS_REGION} || {
      if [ "${AWS_REGION}" == "us-east-1" ]; then
        # us-east-1 não suporta LocationConstraint
        aws s3api create-bucket --bucket ${BUCKET_NAME} --region ${AWS_REGION} || {
          echo -e "${RED}Falha ao criar o bucket S3. Saindo.${NC}"
          exit 1
        }
      else
        echo -e "${RED}Falha ao criar o bucket S3. Saindo.${NC}"
        exit 1
      fi
    }

  # Habilita versioning para o bucket
  echo "Habilitando versionamento do bucket"
  aws s3api put-bucket-versioning --bucket ${BUCKET_NAME} --versioning-configuration Status=Enabled
  
  # Habilita criptografia para o bucket
  echo "Habilitando criptografia padrão do bucket"
  aws s3api put-bucket-encryption --bucket ${BUCKET_NAME} --server-side-encryption-configuration \
    '{"Rules": [{"ApplyServerSideEncryptionByDefault": {"SSEAlgorithm": "AES256"}}]}'
fi

# Cria a tabela do DynamoDB para locks do Terraform se não existir
if aws dynamodb describe-table --table-name ${DYNAMODB_TABLE} &>/dev/null; then
  echo -e "Tabela DynamoDB ${GREEN}${DYNAMODB_TABLE}${NC} já existe"
else
  echo -e "Criando tabela DynamoDB para locks do Terraform: ${GREEN}${DYNAMODB_TABLE}${NC}"
  aws dynamodb create-table \
    --table-name ${DYNAMODB_TABLE} \
    --attribute-definitions AttributeName=LockID,AttributeType=S \
    --key-schema AttributeName=LockID,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region ${AWS_REGION} || {
      echo -e "${RED}Falha ao criar tabela DynamoDB. Saindo.${NC}"
      exit 1
    }
fi

echo -e "${GREEN}Configuração do backend remoto Terraform concluída!${NC}"
echo -e "Agora você pode inicializar o Terraform com:"
echo -e "${YELLOW}terraform init${NC}" 