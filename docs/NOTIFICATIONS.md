# 📢 Guia de Notificações de Deploy

## 🚀 **Configuração Rápida - Slack** (Recomendado)

### **1. Criar Webhook no Slack (2 minutos)**
1. Acesse https://api.slack.com/apps
2. **"Create New App"** → **"From scratch"**
3. Nome: `GitHub Deploys` | Workspace: Seu workspace
4. **"Incoming Webhooks"** → **"Activate Incoming Webhooks"**
5. **"Add New Webhook to Workspace"** → Escolha canal `#deploys`
6. **Copie a URL** (ex: `https://hooks.slack.com/services/...`)

### **2. Configurar Secret no GitHub (1 minuto)**
1. GitHub → Repositório → **Settings** → **Secrets and variables** → **Actions**
2. **"New repository secret"**
3. Name: `SLACK_WEBHOOK_URL`
4. Value: Cole a URL do webhook

### **3. ✅ Pronto!**
As notificações já estão configuradas no workflow `terraform-lambda-deploy.yml`

---

## 🎮 **Discord** (Para developers)

### **Configuração:**
1. Discord → Servidor → **Configurações do Servidor** → **Integrações** → **Webhooks**
2. **"Novo Webhook"** → Canal `#deploys`
3. **"Copiar URL do Webhook"**
4. GitHub Secret: `DISCORD_WEBHOOK`

### **Código:**
```yaml
- name: Discord Notification
  uses: sarisia/actions-status-discord@v1
  with:
    webhook: ${{ secrets.DISCORD_WEBHOOK }}
    title: "JWT Lambda Deploy"
    description: "🚀 Deploy realizado com sucesso!"
```

---

## 📧 **Email** (Simples)

### **Para Gmail:**
1. GitHub Secrets:
   - `EMAIL_USERNAME`: seu.email@gmail.com
   - `EMAIL_PASSWORD`: senha de app (não a senha normal!)
2. Gmail → **Configurações** → **Segurança** → **Senhas de app**

### **Código:**
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

---

## 💼 **Microsoft Teams** (Corporativo)

### **Configuração:**
1. Teams → Canal → **"..."** → **Conectores**
2. **"Incoming Webhook"** → **"Configurar"**
3. Nome: `GitHub Deploys` → **"Criar"**
4. **Copiar URL**
5. GitHub Secret: `TEAMS_WEBHOOK`

---

## 📱 **Telegram** (Rápido e leve)

### **Configuração:**
1. Criar bot: Telegram → @BotFather → `/newbot`
2. Obter token: `123456:ABC-DEF...`
3. Obter Chat ID: Envie `/start` para @userinfobot
4. GitHub Secrets:
   - `TELEGRAM_BOT_TOKEN`: Token do bot
   - `TELEGRAM_CHAT_ID`: Seu chat ID

---

## 🎯 **Comparação Rápida**

| Serviço | Esforço | Popularidade | Recursos |
|---------|---------|--------------|----------|
| **Slack** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Rich formatting, threads, apps |
| **Discord** | ⭐⭐ | ⭐⭐⭐⭐ | Gaming-friendly, embeds |
| **Email** | ⭐ | ⭐⭐⭐ | Universal, HTML support |
| **Teams** | ⭐⭐⭐ | ⭐⭐⭐⭐ | Enterprise, Office 365 |
| **Telegram** | ⭐⭐ | ⭐⭐⭐ | Instant, lightweight |

## 🔥 **Recursos Avançados**

### **Notificações Condicionais:**
```yaml
# Só notifica falhas
- if: failure()

# Só notifica main branch  
- if: github.ref == 'refs/heads/main'

# Só notifica tags de release
- if: startsWith(github.ref, 'refs/tags/')
```

### **Múltiplas Notificações:**
```yaml
# Slack para sucesso, Email para falhas
- name: Success Slack
  if: success()
  uses: 8398a7/action-slack@v3
  
- name: Failure Email
  if: failure()  
  uses: dawidd6/action-send-mail@v3
```

## 🎨 **Personalização**

Edite os templates em `.github/workflows/terraform-lambda-deploy.yml` para:
- 🎨 Cores personalizadas
- 📊 Métricas customizadas  
- 🔗 Links para monitoramento
- 📸 Screenshots de testes
- 📈 Relatórios de performance

**Resultado:** Equipe sempre informada sobre deploys! 🚀 