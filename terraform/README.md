# 🏗️ Terraform Infrastructure - JWT Project

Esta estrutura Terraform provisiona uma infraestrutura completa na AWS para hospedar tanto o **backend-challenge** (API Spring Boot) quanto as **funções AWS Lambda**.

## 📋 Arquitetura

### Componentes Implantados

1. **🌐 VPC e Rede**
   - VPC dedicada com subnets públicas e privadas
   - NAT Gateways para acesso à internet das subnets privadas
   - Security Groups configurados
   - VPC Endpoints para ECR e S3 (otimização de custos)

2. **🚀 Backend API (ECS Fargate)**
   - ECS Cluster com Fargate
   - Application Load Balancer (ALB)
   - Auto Scaling baseado em CPU e memória
   - ECR para armazenar imagens Docker
   - CloudWatch Logs

3. **🗄️ Banco de Dados**
   - RDS PostgreSQL
   - Secrets Manager para credenciais
   - Backup automático

4. **⚡ AWS Lambda**
   - Funções Lambda para staging e production
   - Aliases e versioning
   - CloudWatch Logs

5. **🔐 IAM e Segurança**
   - Roles específicos para ECS e Lambda
   - Usuário CI/CD para GitHub Actions
   - Políticas de menor privilégio

6. **📊 Monitoramento**
   - CloudWatch Metrics e Logs
   - Alarms configurados

## 🚀 Como Usar

### 1. Pré-requisitos

```bash
# Instalar Terraform
curl -fsSL https://apt.releases.hashicorp.com/gpg | sudo apt-key add -
sudo apt-add-repository "deb [arch=amd64] https://apt.releases.hashicorp.com $(lsb_release -cs) main"
sudo apt-get update && sudo apt-get install terraform

# Configurar AWS CLI
aws configure
```

### 2. Configurar Variáveis

Edite o arquivo `terraform.tfvars`:

```hcl
# Configuração Global
aws_region     = "us-east-1"
environment    = "production"
project_owner  = "bapadua.cloud@gmail.com"

# Backend API
backend_api_instance_count = 2
backend_api_cpu           = 512
backend_api_memory        = 1024

# Banco de Dados
db_name             = "jwtdb"
db_username         = "jwtadmin"
db_password         = "SuaSenhaSegura123!"  # MUDE ISTO!
db_instance_class   = "db.t3.micro"
db_allocated_storage = 20

# Lambda
lambda_handler     = "io.github.bapadua.lambda.handler.JwtValidationHandler::handleRequest"
lambda_runtime     = "java21"
lambda_timeout     = 30
lambda_memory_size = 512

# Monitoramento
log_retention_days = 30

# Domínio (opcional)
domain_name     = "api.suaempresa.com"
certificate_arn = "arn:aws:acm:us-east-1:123456789:certificate/abc123"
```

### 3. Deploy da Infraestrutura

```bash
# Inicializar Terraform
terraform init

# Planejar mudanças
terraform plan

# Aplicar infraestrutura
terraform apply
```

### 4. Configurar GitHub Actions

Após o deploy, configure os seguintes secrets no GitHub:

```bash
# Outputs do Terraform mostrarão os valores necessários
terraform output github_actions_secrets
```

### 5. Deploy das Aplicações

As aplicações serão deployadas automaticamente pelos pipelines CI/CD quando:
- **Backend API**: Push para `main` branch do diretório `backend-challenge/`
- **Lambda**: Push para `main` branch do diretório `aws-lambda-jwt/`

## 📁 Estrutura de Módulos

```
terraform/
├── main.tf                    # Configuração principal
├── variables.tf               # Variáveis globais
├── outputs.tf                 # Outputs principais
├── terraform.tfvars           # Valores das variáveis
├── modules/
│   ├── vpc/                   # Rede e segurança
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── backend-api/           # ECS + ALB
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── lambda/                # Funções Lambda
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── rds/                   # Banco de dados
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── iam/                   # Roles e políticas
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── s3/                    # Armazenamento
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── ecr/                   # Container Registry
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   └── cloudwatch/            # Monitoramento
│       ├── main.tf
│       ├── variables.tf
│       └── outputs.tf
└── README.md                  # Este arquivo
```

## 🔗 Integrações

### CI/CD Pipeline

A infraestrutura se integra com os pipelines GitHub Actions:

- **`.github/workflows/backend-api.yml`** - Deploy do backend-challenge
- **`.github/workflows/aws-lambda.yml`** - Deploy das funções Lambda

### Aplicações

- **Backend API**: Roda em ECS Fargate atrás do ALB
- **Lambda**: Funções serverless para validação JWT
- **Database**: PostgreSQL para persistência de dados

## 💰 Estimativa de Custos

| Serviço | Custo Mensal (USD) |
|---------|-------------------|
| ECS Fargate | ~$30-50 |
| ALB | ~$22.50 |
| NAT Gateway | ~$45 |
| RDS (t3.micro) | ~$15-25 |
| Lambda | ~$0-5 |
| S3 + ECR | ~$3-5 |
| **Total** | **~$120-150** |

## 🛡️ Segurança

- ✅ Recursos em subnets privadas
- ✅ Security Groups restritivos
- ✅ IAM roles com menor privilégio
- ✅ Secrets Manager para credenciais
- ✅ VPC Endpoints para reduzir tráfego público
- ✅ Encryption em repouso (RDS, S3)
- ✅ HTTPS/TLS em trânsito

## 🔧 Operações

### Monitoramento

```bash
# Ver logs da API
aws logs tail /ecs/jwt-project-backend-api --follow

# Ver logs do Lambda
aws logs tail /aws/lambda/jwt-project-lambda-production --follow

# Métricas do RDS
aws cloudwatch get-metric-statistics --namespace AWS/RDS \
  --metric-name CPUUtilization --dimensions Name=DBInstanceIdentifier,Value=jwt-project-db \
  --start-time 2024-01-01T00:00:00Z --end-time 2024-01-01T01:00:00Z \
  --period 300 --statistics Average
```

### Scaling

```bash
# Escalar ECS service
aws ecs update-service --cluster jwt-project-backend-api-cluster \
  --service jwt-project-backend-api-service --desired-count 4

# Ver status do auto scaling
aws application-autoscaling describe-scalable-targets \
  --service-namespace ecs
```

### Backup e Disaster Recovery

- **RDS**: Backup automático diário
- **ECR**: Lifecycle policies para limpeza
- **Terraform State**: Recomendado usar S3 backend

## 🚨 Troubleshooting

### Problemas Comuns

1. **ECS tasks não iniciam**
   ```bash
   aws ecs describe-services --cluster jwt-project-backend-api-cluster \
     --services jwt-project-backend-api-service
   ```

2. **Lambda timeout**
   - Aumentar `lambda_timeout` em `terraform.tfvars`
   - Aplicar: `terraform apply`

3. **RDS connection issues**
   - Verificar security groups
   - Testar conectividade do ECS

### Logs Úteis

```bash
# ECS Service Events
aws ecs describe-services --cluster CLUSTER_NAME --services SERVICE_NAME

# Lambda Errors
aws logs filter-log-events --log-group-name /aws/lambda/FUNCTION_NAME \
  --filter-pattern "ERROR"

# ALB Access Logs (se habilitados)
aws s3 ls s3://your-alb-logs-bucket/
```

## 🔄 Atualizações

### Atualizar Infraestrutura

```bash
# Verificar mudanças
terraform plan

# Aplicar atualizações
terraform apply
```

### Rollback

```bash
# Voltar para versão anterior
git checkout HEAD~1
terraform apply

# Ou usar Terraform workspace
terraform workspace select production-backup
terraform apply
```

## 🧹 Limpeza

Para destruir toda a infraestrutura:

```bash
# ⚠️ CUIDADO: Isso remove TUDO!
terraform destroy
```

## 📞 Suporte

- **Logs**: CloudWatch Logs
- **Métricas**: CloudWatch Metrics  
- **Alertas**: CloudWatch Alarms
- **Documentação**: Este README
- **Issues**: GitHub Issues do projeto

---

**🎯 Esta estrutura está pronta para produção e segue as melhores práticas da AWS!** 