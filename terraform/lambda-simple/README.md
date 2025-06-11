# 🚀 JWT Lambda - Estrutura Simples

Esta é uma estrutura **minimalista** do Terraform para criar apenas uma função AWS Lambda para validação JWT.

## 📋 O que é criado

- ✅ **1 função Lambda** (`jwt-validator`)
- ✅ **IAM Role** básico
- ✅ **Function URL** (acesso HTTP direto)
- ✅ **CORS** configurado

## 💰 Custo Estimado

- **~$0-5/mês** (apenas Lambda + logs)

## 🚀 Como usar

### 1. Configurar AWS CLI
```bash
aws configure
```

### 2. Deploy
```bash
cd terraform/lambda-simple
terraform init
terraform plan
terraform apply
```

### 3. Testar
Após o deploy, o Terraform mostrará a URL da função:
```bash
curl -X POST https://abc123.lambda-url.us-east-1.on.aws/ \
  -H "Content-Type: application/json" \
  -d '{"test": "hello"}'
```

### 4. Deploy do código real
```bash
# Compile seu JAR primeiro
aws lambda update-function-code \
  --function-name jwt-validator \
  --zip-file fileb://seu-lambda.jar
```

## 📁 Estrutura

```
terraform/lambda-simple/
├── main.tf           # Recursos principais
├── variables.tf      # Variáveis
├── outputs.tf        # Outputs
├── terraform.tfvars  # Configuração
└── README.md         # Esta documentação
```

## ⚙️ Personalizar

Edite `terraform.tfvars`:
```hcl
aws_region = "us-east-1"
environment = "production"
lambda_handler = "seu.pacote.Handler::handleRequest"
lambda_runtime = "java21"
lambda_timeout = 60
lambda_memory_size = 1024
```

## 🔧 Comandos úteis

```bash
# Ver logs
aws logs tail /aws/lambda/jwt-validator --follow

# Invocar Lambda
aws lambda invoke --function-name jwt-validator response.json

# Destruir tudo
terraform destroy
```

---
**🎯 Estrutura pronta para produção com mínimo de recursos e custos!** 