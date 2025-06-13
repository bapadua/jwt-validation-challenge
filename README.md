## O desenvolvedor

Utilizei cursor ai para o prompt, optei por criar uma lib e utilizar em dois exemplos, api, e lambda.


# JWT Validation API - Documentação Consolidada

Este projeto implementa uma API REST para validação de JSON Web Tokens (JWT) com múltiplas opções de deploy e infraestrutura.

## 📋 Índice

- [Visão Geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Backend Challenge](#backend-challenge)
- [Infraestrutura EKS](#infraestrutura-eks)
- [Infraestrutura Lambda](#infraestrutura-lambda)
- [Deploy com Helm](#deploy-com-helm)
- [Notificações](#notificações)
- [Troubleshooting](#troubleshooting)

## 🎯 Visão Geral

O projeto oferece múltiplas formas de executar a API JWT:

1. **Docker Simples** - Para desenvolvimento e testes rápidos
2. **AWS Lambda** - Para serverless com baixo custo
3. **AWS EKS** - Para produção com Kubernetes
4. **Helm Charts** - Para deploy em qualquer cluster Kubernetes

## 🏗️ Arquitetura

### Componentes Principais

- **API JWT**: Aplicação Spring Boot para validação de tokens
- **Terraform**: Infraestrutura como código para AWS
- **Helm**: Charts para deploy em Kubernetes
- **GitHub Actions**: CI/CD automatizado
- **Monitoramento**: Prometheus/Grafana para métricas

## 🚀 Backend Challenge

### Execução via Docker

#### Pré-requisitos
- Docker 20.10+
- Internet (para download da imagem)

#### Quick Start
```bash
# Executar com imagem do Docker Hub
docker run -d -p 8090:8080 --name backend-api bapadua/backend-api:latest

# Verificar se está funcionando
curl http://localhost:8090/actuator/health
```

#### Build Local (se necessário)
```bash
# Clone do repositório
git clone <repository-url>
cd josewebtoken

# Build da imagem
docker build -f backend-challenge/Dockerfile -t bapadua/backend-api:latest .

# Executar
docker run -d -p 8090:8080 --name backend-api bapadua/backend-api:latest
```

#### Configuração Customizada
```bash
# Executar em porta diferente
docker run -d -p 3000:8080 --name backend-api bapadua/backend-api:latest

# Com variáveis de ambiente
docker run -d \
  -p 8090:8080 \
  -e SERVER_PORT=8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  --name backend-api \
  bapadua/backend-api:latest
```

#### Gerenciamento do Container
```bash
# Verificar status
docker ps

# Ver logs
docker logs backend-api

# Parar e remover
docker stop backend-api && docker rm backend-api
```

### Endpoints Disponíveis

- `GET /actuator/health` - Health check
- `GET /actuator/info` - Informações da aplicação
- `GET /api/jwt/validate-optional` - Endpoint principal

## ☁️ Infraestrutura EKS

### Pré-requisitos

- AWS CLI configurado com credenciais válidas
- Terraform v1.5+
- kubectl instalado
- Helm instalado

### Permissões AWS Necessárias

O usuário ou role IAM deve ter permissões para:
- Criar e gerenciar recursos de VPC
- Gerenciar clusters EKS e grupos de nós
- Configurar load balancers
- Gerenciar serviços de monitoramento
- Configurar backend do Terraform (S3 e DynamoDB)

#### Configuração das Políticas IAM

O projeto inclui políticas IAM divididas em arquivos menores para evitar o limite de 2.048 caracteres das políticas inline. As políticas estão organizadas por serviço:

**Arquivos de Política Disponíveis:**
- `terraform/eks/policy-eks.json` - Permissões para EKS
- `terraform/eks/policy-ec2.json` - Permissões para EC2 (inclui VPC, subnets, etc.)
- `terraform/eks/policy-iam.json` - Permissões para IAM
- `terraform/eks/policy-state.json` - Permissões para S3 e DynamoDB (backend Terraform)
- `terraform/eks/policy-load.json` - Permissões para AutoScaling e Load Balancers

#### Aplicando as Políticas ao Usuário

**Opção 1: Políticas Inline (Recomendado para desenvolvimento)**

```bash
# Navegue para o diretório das políticas
cd terraform/eks

# Aplique cada política separadamente
aws iam put-user-policy \
  --user-name jwt-lambda-cicd-user \
  --policy-name jwt-eks-policy \
  --policy-document file://policy-eks.json

aws iam put-user-policy \
  --user-name jwt-lambda-cicd-user \
  --policy-name jwt-ec2-policy \
  --policy-document file://policy-ec2.json

aws iam put-user-policy \
  --user-name jwt-lambda-cicd-user \
  --policy-name jwt-iam-policy \
  --policy-document file://policy-iam.json

aws iam put-user-policy \
  --user-name jwt-lambda-cicd-user \
  --policy-name jwt-state-policy \
  --policy-document file://policy-state.json

aws iam put-user-policy \
  --user-name jwt-lambda-cicd-user \
  --policy-name jwt-load-policy \
  --policy-document file://policy-load.json
```

**Opção 2: Políticas Gerenciadas (Recomendado para produção)**

```bash
# Crie as políticas gerenciadas
aws iam create-policy \
  --policy-name jwt-eks-policy \
  --policy-document file://policy-eks.json

aws iam create-policy \
  --policy-name jwt-ec2-policy \
  --policy-document file://policy-ec2.json

aws iam create-policy \
  --policy-name jwt-iam-policy \
  --policy-document file://policy-iam.json

aws iam create-policy \
  --policy-name jwt-state-policy \
  --policy-document file://policy-state.json

aws iam create-policy \
  --policy-name jwt-load-policy \
  --policy-document file://policy-load.json

# Anexe as políticas ao usuário (substitua ACCOUNT_ID pela sua conta AWS)
aws iam attach-user-policy \
  --user-name jwt-lambda-cicd-user \
  --policy-arn arn:aws:iam::ACCOUNT_ID:policy/jwt-eks-policy

aws iam attach-user-policy \
  --user-name jwt-lambda-cicd-user \
  --policy-arn arn:aws:iam::ACCOUNT_ID:policy/jwt-ec2-policy

aws iam attach-user-policy \
  --user-name jwt-lambda-cicd-user \
  --policy-arn arn:aws:iam::ACCOUNT_ID:policy/jwt-iam-policy

aws iam attach-user-policy \
  --user-name jwt-lambda-cicd-user \
  --policy-arn arn:aws:iam::ACCOUNT_ID:policy/jwt-state-policy

aws iam attach-user-policy \
  --user-name jwt-lambda-cicd-user \
  --policy-arn arn:aws:iam::ACCOUNT_ID:policy/jwt-load-policy
```

#### Verificando as Políticas Aplicadas

```bash
# Listar políticas inline do usuário
aws iam list-user-policies --user-name jwt-lambda-cicd-user

# Listar políticas gerenciadas anexadas
aws iam list-attached-user-policies --user-name jwt-lambda-cicd-user

# Ver detalhes de uma política específica
aws iam get-user-policy \
  --user-name jwt-lambda-cicd-user \
  --policy-name jwt-eks-policy
```

#### Troubleshooting de Permissões

**Erro: "You are not authorized to perform this operation"**

1. Verifique se o usuário tem as políticas aplicadas:
   ```bash
   aws iam list-user-policies --user-name jwt-lambda-cicd-user
   ```

2. Teste as permissões específicas:
   ```bash
   # Teste permissão EC2
   aws ec2 describe-availability-zones
   
   # Teste permissão EKS
   aws eks list-clusters
   
   # Teste permissão S3
   aws s3 ls s3://jwt-api-terraform-state
   ```

3. Se necessário, adicione permissões específicas que estejam faltando.

#### Criar Política IAM (Método Antigo - Não Recomendado)

### Recursos Criados

- VPC com subnets públicas e privadas
- EKS Cluster com grupos de nós gerenciados
- AWS Load Balancer Controller
- Stack Prometheus/Grafana para monitoramento
- Deployment da API JWT

### Deploy Manual

1. **Inicializar recursos AWS para o backend Terraform**
```bash
chmod +x ./terraform/eks/init-aws.sh
./terraform/eks/init-aws.sh
```

2. **Inicializar o Terraform**
```bash
cd terraform/eks
terraform init
```

3. **Planejar e aplicar**
```bash
terraform plan -var="environment=dev" -var="grafana_admin_password=sua_senha_segura" -out=tfplan
terraform apply tfplan
```

4. **Configurar kubectl**
```bash
aws eks update-kubeconfig --name jwt-api-cluster --region us-east-1
```

### Deploy via GitHub Actions

Use o workflow `eks-deploy.yml` que pode ser acionado:
- Automático ao fazer push no branch main
- Manual via GitHub Actions UI

#### Secrets necessários no GitHub
- `AWS_ACCESS_KEY_ID`: ID da chave de acesso AWS
- `AWS_SECRET_ACCESS_KEY`: Chave secreta de acesso AWS
- `GRAFANA_ADMIN_PASSWORD`: Senha para o usuário admin do Grafana

### Monitoramento

#### Dashboards Inclusos
- **JWT API Dashboard**: Métricas específicas da API JWT
- **JVM Dashboard**: Métricas da JVM para aplicações Spring Boot
- **Kubernetes Dashboards**: Estado do cluster, nós e pods

#### Acessando o Grafana
Após a implantação, o Grafana estará disponível em:
```
https://monitoring.jwt-demo.com
```

Credenciais padrão:
- **Usuário**: admin
- **Senha**: (definida na variável `grafana_admin_password`)

#### Métricas Coletadas
A aplicação expõe métricas via endpoint Prometheus em `/actuator/prometheus`:
- Taxa de requisições
- Latência por endpoint
- Códigos de status HTTP
- Uso de memória e CPU
- Tempo de resposta
- Erros/Exceções

### Limpeza de Recursos
```bash
terraform destroy -var="environment=dev" -var="grafana_admin_password=sua_senha_segura"
```

### Troubleshooting do Terraform

#### 🔒 Problema: Lock do Estado Terraform

**Erro**: `Error acquiring the state lock` ou `ConditionalCheckFailedException`

Este erro ocorre quando uma operação anterior do Terraform não foi finalizada corretamente ou quando múltiplas execuções tentam acessar o estado simultaneamente.

**Solução Rápida (Recomendada):**
1. Acesse o **Console AWS** → **DynamoDB**
2. Vá para a tabela `jwt-api-terraform-locks`
3. Clique em **"Explorar itens de tabela"**
4. Procure pelo item com **LockID**: `jwt-api-terraform-state/eks/terraform.tfstate`
5. Selecione o item e clique em **"Excluir"**

**Solução via AWS CLI:**
```bash
aws dynamodb delete-item \
  --table-name jwt-api-terraform-locks \
  --key '{"LockID":{"S":"jwt-api-terraform-state/eks/terraform.tfstate"}}'
```

**Solução via Script:**
```bash
cd terraform/eks
./force-unlock.sh
```

**Prevenção:**
- O workflow do GitHub Actions inclui limpeza automática de locks
- Evite executar múltiplos pipelines simultaneamente
- Aguarde a conclusão de um deploy antes de iniciar outro

#### Problema: Permissões Insuficientes

**Erro**: `You are not authorized to perform this operation`

1. **Verifique se as políticas IAM foram aplicadas**:
   ```bash
   aws iam list-user-policies --user-name jwt-lambda-cicd-user
   ```

2. **Teste permissões específicas**:
   ```bash
   # EC2
   aws ec2 describe-availability-zones
   
   # EKS
   aws eks list-clusters
   
   # S3
   aws s3 ls s3://jwt-api-terraform-state
   ```

3. **Reaplique as políticas se necessário** (veja seção "Configuração das Políticas IAM")

#### Problema: Build Multi-Módulo Maven

**Erro**: `Could not find artifact jwt-validation-lib`

O projeto é um multi-módulo Maven. Certifique-se de executar o build na raiz:
```bash
# ✅ Correto - na raiz do projeto
mvn clean install -DskipTests

# ❌ Incorreto - apenas no backend-challenge
cd backend-challenge && mvn clean package -DskipTests
```

#### Problema: Timeout no Deploy

**Sintomas**: Deploy trava ou falha após muito tempo

1. **Verifique recursos AWS**:
   ```bash
   # Verificar se o cluster EKS está saudável
   aws eks describe-cluster --name jwt-api-cluster
   
   # Verificar nós do cluster
   kubectl get nodes
   ```

2. **Verifique logs do Kubernetes**:
   ```bash
   # Logs dos pods da API
   kubectl logs -n jwt-api -l app=jwt-api
   
   # Logs do load balancer controller
   kubectl logs -n kube-system -l app.kubernetes.io/name=aws-load-balancer-controller
   ```

#### Comandos Úteis para Debug

```bash
# Verificar estado do Terraform
terraform show

# Verificar outputs
terraform output

# Verificar recursos criados
terraform state list

# Verificar logs do workflow
# (no GitHub Actions, clique em "View workflow run" > "Build" > "View logs")

# Verificar cluster EKS
aws eks describe-cluster --name jwt-api-cluster --query 'cluster.status'

# Verificar ingress
kubectl get ingress -n jwt-api
kubectl describe ingress -n jwt-api
```

## ⚡ Infraestrutura Lambda

### Estrutura Minimalista

Esta é uma estrutura **minimalista** do Terraform para criar apenas uma função AWS Lambda para validação JWT.

#### O que é criado
- ✅ **1 função Lambda** (`jwt-validator`)
- ✅ **IAM Role** básico
- ✅ **Function URL** (acesso HTTP direto)
- ✅ **CORS** configurado

#### Custo Estimado
- **~$0-5/mês** (apenas Lambda + logs)

### Deploy

1. **Configurar AWS CLI**
```bash
aws configure
```

2. **Deploy**
```bash
cd terraform/lambda-simple
terraform init
terraform plan
terraform apply
```

3. **Testar**
Após o deploy, o Terraform mostrará a URL da função:
```bash
curl -X POST https://abc123.lambda-url.us-east-1.on.aws/ \
  -H "Content-Type: application/json" \
  -d '{"test": "hello"}'
```

4. **Deploy do código real**
```bash
# Compile seu JAR primeiro
aws lambda update-function-code \
  --function-name jwt-validator \
  --zip-file fileb://seu-lambda.jar
```

### Personalização

Edite `terraform/lambda-simple/terraform.tfvars`:
```hcl
aws_region = "us-east-1"
environment = "production"
lambda_handler = "seu.pacote.Handler::handleRequest"
lambda_runtime = "java21"
lambda_timeout = 60
lambda_memory_size = 1024
```

### Comandos Úteis
```bash
# Ver logs
aws logs tail /aws/lambda/jwt-validator --follow

# Invocar Lambda
aws lambda invoke --function-name jwt-validator response.json

# Destruir tudo
terraform destroy
```

## 🎯 Deploy com Helm

### Pré-requisitos

- Kubernetes 1.19+
- Helm 3.2.0+
- Cluster com pelo menos 1 GB de RAM disponível

### Instalação Básica

```bash
# Verificar o template
helm template jwt-api ./backend-challenge/helm/jwt-validation-api

# Instalar com configurações padrão
helm install jwt-api ./backend-challenge/helm/jwt-validation-api

# Ou com namespace específico
helm install jwt-api ./backend-challenge/helm/jwt-validation-api --namespace jwt-system --create-namespace
```

### Instalação com Script Helper

```bash
# Instalar com script auxiliar
./backend-challenge/helm/install.sh

# Especificar namespace
./backend-challenge/helm/install.sh -n jwt-system

# Fazer dry-run
./backend-challenge/helm/install.sh --dry-run
```

### Instalação com Ingress

```bash
# Instalação básica com ingress habilitado
./backend-challenge/helm/install.sh -i

# Configurar hostname do ingress
./backend-challenge/helm/install.sh -i --ingress-host jwt-api.meudominio.com

# Habilitar TLS (requer cert-manager)
./backend-challenge/helm/install.sh -i --ingress-host jwt-api.meudominio.com --ingress-tls

# Especificar classe do ingress
./backend-challenge/helm/install.sh -i --ingress-class istio
```

### Configurações Principais

| Parâmetro | Descrição | Valor Padrão |
|-----------|-----------|--------------|
| `replicaCount` | Número de réplicas | `2` |
| `image.repository` | Repositório da imagem | `bapadua/backend-api` |
| `image.tag` | Tag da imagem | `latest` |
| `service.type` | Tipo do service | `ClusterIP` |
| `service.port` | Porta do service | `80` |
| `ingress.enabled` | Habilitar ingress | `false` |
| `autoscaling.enabled` | Habilitar HPA | `true` |
| `monitoring.enabled` | Habilitar monitoring | `false` |

### Recursos e Limites

```yaml
resources:
  limits:
    cpu: 500m
    memory: 512Mi
  requests:
    cpu: 250m
    memory: 256Mi
```

### Acesso à Aplicação

#### Port Forward (Desenvolvimento)
```bash
kubectl port-forward svc/jwt-api-jwt-validation-api 8080:80
```
Depois acesse: http://localhost:8080

#### Via Ingress
```bash
# Configuração básica de ingress
./backend-challenge/helm/install.sh -i --ingress-host jwt-api.exemplo.com

# Com TLS habilitado
./backend-challenge/helm/install.sh -i --ingress-host jwt-api.exemplo.com --ingress-tls
```

### Comandos Úteis

```bash
# Status do release
helm status jwt-api

# Logs da aplicação
kubectl logs -l app.kubernetes.io/name=jwt-validation-api

# Atualizar com novos valores
helm upgrade jwt-api ./backend-challenge/helm/jwt-validation-api \
  --set image.tag=v1.1.0

# Remover o release
helm uninstall jwt-api
```

## 📢 Notificações

### Configuração Rápida - Slack (Recomendado)

#### 1. Criar Webhook no Slack (2 minutos)
1. Acesse https://api.slack.com/apps
2. **"Create New App"** → **"From scratch"**
3. Nome: `GitHub Deploys` | Workspace: Seu workspace
4. **"Incoming Webhooks"** → **"Activate Incoming Webhooks"**
5. **"Add New Webhook to Workspace"** → Escolha canal `#deploys`
6. **Copie a URL** (ex: `https://hooks.slack.com/services/...`)

#### 2. Configurar Secret no GitHub (1 minuto)
1. GitHub → Repositório → **Settings** → **Secrets and variables** → **Actions**
2. **"New repository secret"**
3. Name: `SLACK_WEBHOOK_URL`
4. Value: Cole a URL do webhook

### Outras Opções

#### Discord
```yaml
- name: Discord Notification
  uses: sarisia/actions-status-discord@v1
  with:
    webhook: ${{ secrets.DISCORD_WEBHOOK }}
    title: "JWT Lambda Deploy"
    description: "🚀 Deploy realizado com sucesso!"
```

#### Email (Gmail)
```yaml
- name: Send Email
  uses: dawidd6/action-send-mail@v3
  with:
    server_address: smtp.gmail.com
    username: ${{ secrets.EMAIL_USERNAME }}
    password: ${{ secrets.EMAIL_PASSWORD }}
    to: dev-team@empresa.com
    subject: "🚀 Deploy Concluído!"
```

#### Microsoft Teams
1. Teams → Canal → **"..."** → **Conectores**
2. **"Incoming Webhook"** → **"Configurar"**
3. Nome: `GitHub Deploys` → **"Criar"**
4. **Copiar URL**
5. GitHub Secret: `TEAMS_WEBHOOK`

#### Telegram
1. Criar bot: Telegram → @BotFather → `/newbot`
2. Obter token: `123456:ABC-DEF...`
3. Obter Chat ID: Envie `/start` para @userinfobot
4. GitHub Secrets:
   - `TELEGRAM_BOT_TOKEN`: Token do bot
   - `TELEGRAM_CHAT_ID`: Seu chat ID

### Comparação Rápida

| Serviço | Esforço | Popularidade | Recursos |
|---------|---------|--------------|----------|
| **Slack** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Rich formatting, threads, apps |
| **Discord** | ⭐⭐ | ⭐⭐⭐⭐ | Gaming-friendly, embeds |
| **Email** | ⭐ | ⭐⭐⭐ | Universal, HTML support |
| **Teams** | ⭐⭐⭐ | ⭐⭐⭐⭐ | Enterprise, Office 365 |
| **Telegram** | ⭐⭐ | ⭐⭐⭐ | Instant, lightweight |

## 🔍 Troubleshooting

### Docker

#### Porta já em uso
```bash
# Verificar o que está usando a porta
netstat -tlnp | grep 8090

# Usar porta diferente
docker run -d -p 8091:8080 --name backend-api bapadua/backend-api:latest
```

#### Container não inicia
```bash
# Ver logs detalhados
docker logs backend-api

# Executar em modo interativo para debug
docker run -it --rm -p 8090:8080 bapadua/backend-api:latest

# Verificar se a imagem existe
docker images | grep bapadua/backend-api
```

#### Problemas de conectividade
```bash
# Testar conectividade
curl -v http://localhost:8090/actuator/health

# Verificar dentro do container
docker exec -it backend-api curl localhost:8080/actuator/health

# Verificar se o Docker está rodando
docker ps
```

### Kubernetes/Helm

#### Pod não inicia
```bash
# Verificar eventos
kubectl describe pod <pod-name>

# Verificar logs
kubectl logs <pod-name>

# Verificar configurações
kubectl get pod <pod-name> -o yaml
```

#### Problemas de conectividade
```bash
# Testar conectividade dentro do cluster
kubectl run test-pod --image=curlimages/curl --rm -it -- /bin/sh
curl http://jwt-api-jwt-validation-api/actuator/health
```

#### Problemas de recursos
```bash
# Verificar recursos do cluster
kubectl top nodes
kubectl top pods

# Verificar limites
kubectl describe limitrange
kubectl describe resourcequota
```

#### Problemas com Ingress
```bash
# Verificar configuração do ingress
kubectl get ingress -l app.kubernetes.io/name=jwt-validation-api -o yaml

# Verificar eventos do ingress
kubectl describe ingress <ingress-name>

# Verificar logs do controlador de ingress
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx
```

### AWS/Terraform

#### Problemas de permissões
```bash
# Verificar credenciais AWS
aws sts get-caller-identity

# Testar permissões específicas
aws eks describe-cluster --name jwt-api-cluster --region us-east-1
```

#### Problemas de rede
```bash
# Verificar VPC e subnets
aws ec2 describe-vpcs
aws ec2 describe-subnets

# Verificar security groups
aws ec2 describe-security-groups
```

## 📁 Estrutura do Projeto

```
josewebtoken/
├── backend-challenge/           # Aplicação Spring Boot
│   ├── Dockerfile              # Docker para a aplicação
│   └── helm/                   # Charts Helm
│       └── jwt-validation-api/ # Chart para Kubernetes
├── terraform/                  # Infraestrutura como código
│   ├── eks/                    # Infraestrutura EKS
│   │   ├── main.tf            # Definição principal
│   │   ├── variables.tf       # Variáveis
│   │   ├── outputs.tf         # Outputs
│   │   ├── init-aws.sh        # Script de inicialização
│   │   └── aws-terraform-policy.json # Política IAM
│   └── lambda-simple/         # Infraestrutura Lambda
│       ├── main.tf            # Recursos Lambda
│       ├── variables.tf       # Variáveis
│       └── outputs.tf         # Outputs
├── .github/workflows/         # CI/CD
│   ├── eks-deploy.yml         # Deploy EKS
│   ├── lambda-deploy.yml      # Deploy Lambda
│   └── terraform-destroy.yml  # Limpeza de recursos
└── docs/                      # Documentação adicional
    └── NOTIFICATIONS.md       # Guia de notificações
```

## 🎯 Quick Start

### Para Desenvolvimento
```bash
docker run -d -p 8090:8080 --name backend-api bapadua/backend-api:latest
curl http://localhost:8090/actuator/health
```

### Para Produção com EKS
```bash
cd terraform/eks
./init-aws.sh
terraform init
terraform apply -var="environment=prod" -var="grafana_admin_password=sua_senha_segura"
```

### Para Serverless com Lambda
```bash
cd terraform/lambda-simple
terraform init
terraform apply
```

### Para Kubernetes com Helm
```bash
./backend-challenge/helm/install.sh -i --ingress-host jwt-api.meudominio.com
```

---

**🎯 Projeto pronto para produção com múltiplas opções de deploy!** 