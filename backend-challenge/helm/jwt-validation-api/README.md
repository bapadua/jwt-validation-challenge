# Chart Helm - JWT Validation API

Este chart Helm permite deployar a JWT Validation API no Kubernetes de forma simples e configurável.

## 📋 Pré-requisitos

- Kubernetes 1.19+
- Helm 3.2.0+
- Cluster com pelo menos 1 GB de RAM disponível

## 🚀 Instalação

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

### Instalação com Configurações Customizadas

```bash
# Instalação com configurações personalizadas
helm install jwt-api ./backend-challenge/helm/jwt-validation-api \
  --set image.tag=v1.0.0 \
  --set replicaCount=3 \
  --set ingress.enabled=true \
  --set ingress.hosts[0].host=jwt-api.meudominio.com
```

## ⚙️ Configurações

### Principais Parâmetros

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

### Opções do Script de Instalação

| Opção | Descrição | Valor Padrão |
|-------|-----------|--------------|
| `-n, --namespace` | Namespace para instalação | `default` |
| `-r, --release` | Nome do release | `jwt-api` |
| `-i, --ingress` | Habilitar ingress | `false` |
| `--ingress-host` | Hostname para o ingress | `jwt-api.local` |
| `--ingress-tls` | Habilitar TLS no ingress | `false` |
| `--ingress-class` | Classe do ingress | `nginx` |
| `--dry-run` | Executar dry-run | - |
| `--debug` | Habilitar debug | - |

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

### Configurações de Health Check

```yaml
healthcheck:
  enabled: true
  livenessProbe:
    httpGet:
      path: /actuator/health
      port: http
    initialDelaySeconds: 30
    periodSeconds: 30
  readinessProbe:
    httpGet:
      path: /actuator/health
      port: http
    initialDelaySeconds: 10
    periodSeconds: 10
```

## 🔧 Comandos Úteis

### Verificar Status

```bash
# Status do release
helm status jwt-api

# Listar todos os releases
helm list

# Pods da aplicação
kubectl get pods -l app.kubernetes.io/name=jwt-validation-api
```

### Logs e Debug

```bash
# Logs da aplicação
kubectl logs -l app.kubernetes.io/name=jwt-validation-api

# Debug do template
helm template jwt-api ./backend-challenge/helm/jwt-validation-api --debug

# Dry run da instalação
helm install jwt-api ./backend-challenge/helm/jwt-validation-api --dry-run --debug
```

### Atualizações

```bash
# Atualizar com novos valores
helm upgrade jwt-api ./backend-challenge/helm/jwt-validation-api \
  --set image.tag=v1.1.0

# Rollback para versão anterior
helm rollback jwt-api 1

# Histórico de releases
helm history jwt-api
```

### Desinstalação

```bash
# Remover o release
helm uninstall jwt-api

# Remover release e namespace
helm uninstall jwt-api --namespace jwt-system
kubectl delete namespace jwt-system
```

## 🌐 Acesso à Aplicação

### Port Forward (Desenvolvimento)

```bash
kubectl port-forward svc/jwt-api-jwt-validation-api 8080:80
```

Depois acesse: http://localhost:8080

### Via Ingress

Configure o ingress no `values.yaml` ou durante a instalação com o script:

```bash
# Configuração básica de ingress
./backend-challenge/helm/install.sh -i --ingress-host jwt-api.exemplo.com

# Com TLS habilitado
./backend-challenge/helm/install.sh -i --ingress-host jwt-api.exemplo.com --ingress-tls
```

Ou manualmente:

```yaml
ingress:
  enabled: true
  hosts:
    - host: jwt-api.exemplo.com
      paths:
        - path: /
          pathType: Prefix
```

### Endpoints Disponíveis

- `GET /actuator/health` - Health check
- `GET /actuator/info` - Informações da aplicação
- `GET /api/jwt/validate-optional` - Endpoint principal

## 📊 Monitoramento

### Prometheus

Para habilitar o ServiceMonitor:

```yaml
monitoring:
  enabled: true
  serviceMonitor:
    enabled: true
    interval: 30s
    path: /actuator/prometheus
```

### Métricas Disponíveis

A aplicação expõe métricas via Spring Boot Actuator no endpoint `/actuator/prometheus`.

## 🔒 Segurança

O chart inclui configurações de segurança por padrão:

- Execução como usuário não-root
- Security contexts configurados
- Capabilities limitadas
- Resource limits definidos

## 🐛 Troubleshooting

### Pod não inicia

```bash
# Verificar eventos
kubectl describe pod <pod-name>

# Verificar logs
kubectl logs <pod-name>

# Verificar configurações
kubectl get pod <pod-name> -o yaml
```

### Problemas de conectividade

```bash
# Testar conectividade dentro do cluster
kubectl run test-pod --image=curlimages/curl --rm -it -- /bin/sh
curl http://jwt-api-jwt-validation-api/actuator/health
```

### Problemas de recursos

```bash
# Verificar recursos do cluster
kubectl top nodes
kubectl top pods

# Verificar limites
kubectl describe limitrange
kubectl describe resourcequota
```

### Problemas com Ingress

```bash
# Verificar configuração do ingress
kubectl get ingress -l app.kubernetes.io/name=jwt-validation-api -o yaml

# Verificar eventos do ingress
kubectl describe ingress <ingress-name>

# Verificar logs do controlador de ingress
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx
``` 