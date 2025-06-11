# 🚀 Guia Completo: Deploy do JWT Lambda na AWS

Este guia te levará do zero ao Lambda funcionando em produção com CI/CD automatizado.

## 📋 Pré-requisitos

- ✅ Conta AWS ativa
- ✅ AWS CLI instalado
- ✅ Java 21 instalado
- ✅ Maven configurado
- ✅ Projeto buildado localmente

## 🛠️ Passo 1: Configurar AWS CLI

### **1.1 Instalar AWS CLI**
```bash
# Windows (PowerShell)
msiexec.exe /i https://awscli.amazonaws.com/AWSCLIV2.msi

# MacOS
brew install awscli

# Linux
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

### **1.2 Configurar Credenciais**
```bash
aws configure
```

**Informações necessárias:**
- **AWS Access Key ID**: Sua chave de acesso
- **AWS Secret Access Key**: Sua chave secreta  
- **Default region**: `us-east-1` (recomendado para Lambda)
- **Default output format**: `json`

### **1.3 Verificar Configuração**
```bash
aws sts get-caller-identity
```

## 🔧 Passo 2: Criar IAM Role para Lambda

### **2.1 Criar Trust Policy**
```bash
cat > lambda-trust-policy.json << EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
```

### **2.2 Criar IAM Role**
```bash
aws iam create-role \
  --role-name jwt-lambda-execution-role \
  --assume-role-policy-document file://lambda-trust-policy.json
```

### **2.3 Anexar Políticas Básicas**
```bash
# Política básica de execução Lambda
aws iam attach-role-policy \
  --role-name jwt-lambda-execution-role \
  --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole

# Política para CloudWatch (opcional, para logs detalhados)
aws iam attach-role-policy \
  --role-name jwt-lambda-execution-role \
  --policy-arn arn:aws:iam::aws:policy/CloudWatchFullAccess
```

### **2.4 Obter ARN da Role (salve este valor!)**
```bash
aws iam get-role --role-name jwt-lambda-execution-role --query 'Role.Arn' --output text
```

## 📦 Passo 3: Buildar o Lambda Package

### **3.1 Build do Projeto**
```bash
cd aws-lambda-jwt
mvn clean package -DskipTests
```

### **3.2 Verificar JAR Gerado**
```bash
ls -la target/
# Procure por: aws-lambda-jwt-*.jar
```

## 🚀 Passo 4: Criar Funções Lambda

### **4.1 Criar Lambda de Staging**
```bash
aws lambda create-function \
  --function-name jwt-validator-staging \
  --runtime java21 \
  --role arn:aws:iam::SEU_ACCOUNT_ID:role/jwt-lambda-execution-role \
  --handler io.github.bapadua.jwt.lambda.JwtValidationHandler::handleRequest \
  --zip-file fileb://target/aws-lambda-jwt-*.jar \
  --timeout 30 \
  --memory-size 512 \
  --environment Variables='{
    "ENVIRONMENT": "staging",
    "LOG_LEVEL": "DEBUG"
  }'
```

### **4.2 Criar Lambda de Production**
```bash
aws lambda create-function \
  --function-name jwt-validator-production \
  --runtime java21 \
  --role arn:aws:iam::SEU_ACCOUNT_ID:role/jwt-lambda-execution-role \
  --handler io.github.bapadua.jwt.lambda.JwtValidationHandler::handleRequest \
  --zip-file fileb://target/aws-lambda-jwt-*.jar \
  --timeout 30 \
  --memory-size 512 \
  --environment Variables='{
    "ENVIRONMENT": "production",
    "LOG_LEVEL": "INFO"
  }'
```

### **4.3 Criar Alias de Production**
```bash
# Publicar primeira versão
aws lambda publish-version --function-name jwt-validator-production

# Criar alias PROD apontando para versão 1
aws lambda create-alias \
  --function-name jwt-validator-production \
  --name PROD \
  --function-version 1
```

## 📂 Passo 5: Criar S3 Bucket para Artifacts

### **5.1 Criar Bucket**
```bash
# Substitua por um nome único
BUCKET_NAME="jwt-lambda-artifacts-$(date +%s)"

aws s3 mb s3://$BUCKET_NAME

echo "Bucket criado: $BUCKET_NAME"
# SALVE ESTE NOME DO BUCKET!
```

### **5.2 Configurar Versionamento**
```bash
aws s3api put-bucket-versioning \
  --bucket $BUCKET_NAME \
  --versioning-configuration Status=Enabled
```

## 🔧 Passo 6: Testar Lambda Localmente

### **6.1 Criar Payload de Teste**
```bash
cat > test-payload.json << EOF
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJSb2xlIjoi",
  "requestId": "test-request-123"
}
EOF
```

### **6.2 Testar Staging**
```bash
aws lambda invoke \
  --function-name jwt-validator-staging \
  --payload file://test-payload.json \
  response-staging.json

cat response-staging.json
```

### **6.3 Testar Production**
```bash
aws lambda invoke \
  --function-name jwt-validator-production:PROD \
  --payload file://test-payload.json \
  response-production.json

cat response-production.json
```

## 🔐 Passo 7: Configurar GitHub Secrets

Vá para seu repositório GitHub → **Settings** → **Secrets and variables** → **Actions**

### **7.1 Secrets AWS**
```
AWS_ACCESS_KEY_ID = sua_access_key
AWS_SECRET_ACCESS_KEY = sua_secret_key
LAMBDA_FUNCTION_NAME_STAGING = jwt-validator-staging
LAMBDA_FUNCTION_NAME_PROD = jwt-validator-production
LAMBDA_DEPLOYMENT_BUCKET = nome_do_bucket_criado
```

### **7.2 Obter Valores**
```bash
# Para obter seu Account ID
aws sts get-caller-identity --query Account --output text

# Para listar suas funções Lambda
aws lambda list-functions --query 'Functions[].FunctionName'

# Para listar seus buckets S3
aws s3 ls
```

## 🏗️ Passo 8: Configurar GitHub Environments

### **8.1 Criar Environment "staging"**
1. Vá para **Settings** → **Environments**
2. Clique **New environment**
3. Nome: `staging`
4. **Protection rules**: Deixe vazio (deploy automático)

### **8.2 Criar Environment "production"**
1. Nome: `production`
2. **Protection rules**: 
   - ✅ **Required reviewers**: Adicione você mesmo
   - ✅ **Wait timer**: 0 minutes
   - ✅ **Prevent self-review**: Desmarque se for só você

## 🧪 Passo 9: Testar Pipeline

### **9.1 Fazer Mudança no Lambda**
```bash
cd aws-lambda-jwt/src/main/java/io/github/bapadua/jwt/lambda

# Adicionar um comentário qualquer no código
echo "// Pipeline test $(date)" >> JwtValidationHandler.java
```

### **9.2 Commit e Push**
```bash
git add .
git commit -m "test: trigger lambda pipeline"
git push origin main
```

### **9.3 Acompanhar Pipeline**
1. Vá para **Actions** no GitHub
2. Verifique se o workflow **AWS Lambda CI/CD** foi executado
3. Acompanhe os logs de cada step

## 📊 Passo 10: Monitoramento e Logs

### **10.1 Ver Logs CloudWatch**
```bash
# Logs de staging
aws logs describe-log-groups --log-group-name-prefix "/aws/lambda/jwt-validator-staging"

# Ver logs recentes
aws logs tail /aws/lambda/jwt-validator-staging --follow
```

### **10.2 Métricas CloudWatch**
```bash
# Ver invocações das últimas 24h
aws cloudwatch get-metric-statistics \
  --namespace AWS/Lambda \
  --metric-name Invocations \
  --dimensions Name=FunctionName,Value=jwt-validator-production \
  --statistics Sum \
  --start-time $(date -u -d '24 hours ago' +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 3600
```

## 🛡️ Passo 11: Configurar API Gateway (Opcional)

### **11.1 Criar API Gateway**
```bash
aws apigateway create-rest-api --name jwt-validation-api
```

### **11.2 Obter API ID**
```bash
API_ID=$(aws apigateway get-rest-apis --query 'items[?name==`jwt-validation-api`].id' --output text)
echo "API ID: $API_ID"
```

### **11.3 Criar Resource e Method**
```bash
# Obter Root Resource ID
ROOT_ID=$(aws apigateway get-resources --rest-api-id $API_ID --query 'items[?path==`/`].id' --output text)

# Criar resource /validate
aws apigateway create-resource \
  --rest-api-id $API_ID \
  --parent-id $ROOT_ID \
  --path-part validate

# Obter Resource ID
RESOURCE_ID=$(aws apigateway get-resources --rest-api-id $API_ID --query 'items[?pathPart==`validate`].id' --output text)

# Criar método POST
aws apigateway put-method \
  --rest-api-id $API_ID \
  --resource-id $RESOURCE_ID \
  --http-method POST \
  --authorization-type NONE
```

## 🔧 Comandos Úteis

### **Debug e Troubleshooting**
```bash
# Ver detalhes da função
aws lambda get-function --function-name jwt-validator-production

# Ver configuração
aws lambda get-function-configuration --function-name jwt-validator-production

# Ver versões
aws lambda list-versions-by-function --function-name jwt-validator-production

# Ver aliases
aws lambda list-aliases --function-name jwt-validator-production

# Atualizar código manualmente (em caso de emergência)
aws lambda update-function-code \
  --function-name jwt-validator-production \
  --zip-file fileb://target/aws-lambda-jwt-*.jar
```

### **Limpeza (se necessário)**
```bash
# Deletar função Lambda
aws lambda delete-function --function-name jwt-validator-staging

# Deletar role IAM
aws iam detach-role-policy --role-name jwt-lambda-execution-role --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole
aws iam delete-role --role-name jwt-lambda-execution-role

# Deletar bucket S3 (cuidado!)
aws s3 rb s3://seu-bucket-name --force
```

## ✅ Checklist Final

- [ ] AWS CLI configurado
- [ ] IAM Role criada
- [ ] Lambda Staging criado
- [ ] Lambda Production criado
- [ ] Alias PROD configurado
- [ ] S3 Bucket criado
- [ ] GitHub Secrets configurados
- [ ] GitHub Environments configurados
- [ ] Pipeline testado e funcionando
- [ ] Logs CloudWatch verificados
- [ ] Monitoramento ativo

## 🎉 Resultado Final

Após seguir este guia, você terá:

✅ **Lambda Functions** deployadas em staging e production  
✅ **CI/CD Pipeline** totalmente automatizado  
✅ **Monitoramento** com CloudWatch e alertas  
✅ **Security** com IAM roles adequadas  
✅ **Versionamento** automático de deployments  
✅ **Rollback** capability com aliases  

**Seu Lambda está pronto para produção! 🚀**

---

## 📞 Precisa de Ajuda?

Se algo der errado, verifique:
1. **Logs CloudWatch** da função Lambda
2. **Actions logs** no GitHub
3. **IAM permissions** da role
4. **Network connectivity** se usando VPC

**Boa sorte com seu deploy! 🍀** 