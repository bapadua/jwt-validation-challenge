#!/bin/bash

# Script de instalação para JWT Validation API
# Uso: ./install.sh [opções]

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log
log() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verificar se o Helm está instalado
check_helm() {
    if ! command -v helm &> /dev/null; then
        error "Helm não está instalado. Por favor, instale o Helm primeiro."
        exit 1
    fi
    log "Helm encontrado: $(helm version --short)"
}

# Verificar se o kubectl está instalado e configurado
check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        error "kubectl não está instalado. Por favor, instale o kubectl primeiro."
        exit 1
    fi
    
    if ! kubectl cluster-info &> /dev/null; then
        error "kubectl não está configurado ou cluster não está acessível."
        exit 1
    fi
    log "kubectl configurado e cluster acessível"
}

# Mostrar ajuda
show_help() {
    echo "Script de instalação da JWT Validation API"
    echo ""
    echo "Uso: $0 [OPTIONS]"
    echo ""
    echo "OPTIONS:"
    echo "  -n, --namespace NAMESPACE    Namespace para instalação (padrão: default)"
    echo "  -r, --release RELEASE        Nome do release (padrão: jwt-api)"
    echo "  -i, --ingress                Habilitar ingress"
    echo "  --ingress-host HOST          Hostname para o ingress (padrão: jwt-api.local)"
    echo "  --ingress-tls                Habilitar TLS no ingress"
    echo "  --ingress-class CLASS        Classe do ingress (padrão: nginx)"
    echo "  -h, --help                   Mostrar esta ajuda"
    echo "  --dry-run                    Executar dry-run"
    echo "  --debug                      Habilitar debug"
    echo ""
    echo "Exemplos:"
    echo "  $0 -n jwt-system"
    echo "  $0 -i --ingress-host api.exemplo.com"
    echo "  $0 -i --ingress-host api.exemplo.com --ingress-tls"
    echo "  $0 --dry-run"
}

# Configurações padrão
NAMESPACE="default"
RELEASE="jwt-api"
DRY_RUN=""
DEBUG=""
INGRESS_ENABLED="false"
INGRESS_HOST="jwt-api.local"
INGRESS_TLS="false"
INGRESS_CLASS="nginx"
SET_VALUES=""

# Parse dos argumentos
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -r|--release)
            RELEASE="$2"
            shift 2
            ;;
        -i|--ingress)
            INGRESS_ENABLED="true"
            shift
            ;;
        --ingress-host)
            INGRESS_HOST="$2"
            shift 2
            ;;
        --ingress-tls)
            INGRESS_TLS="true"
            shift
            ;;
        --ingress-class)
            INGRESS_CLASS="$2"
            shift 2
            ;;
        --dry-run)
            DRY_RUN="--dry-run"
            shift
            ;;
        --debug)
            DEBUG="--debug"
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            error "Argumento desconhecido: $1"
            show_help
            exit 1
            ;;
    esac
done

# Diretório do script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHART_DIR="$SCRIPT_DIR/jwt-validation-api"

log "Iniciando instalação da JWT Validation API"
log "Namespace: $NAMESPACE" 
log "Release: $RELEASE"

# Configuração do Ingress
if [ "$INGRESS_ENABLED" = "true" ]; then
    log "Ingress habilitado para host: $INGRESS_HOST"
    SET_VALUES="$SET_VALUES --set ingress.enabled=true"
    SET_VALUES="$SET_VALUES --set ingress.className=$INGRESS_CLASS"
    SET_VALUES="$SET_VALUES --set ingress.hosts[0].host=$INGRESS_HOST"
    
    if [ "$INGRESS_TLS" = "true" ]; then
        log "TLS habilitado para ingress"
        SECRET_NAME="$(echo $INGRESS_HOST | sed 's/\./-/g')-tls"
        SET_VALUES="$SET_VALUES --set ingress.tls[0].secretName=$SECRET_NAME"
        SET_VALUES="$SET_VALUES --set ingress.tls[0].hosts[0]=$INGRESS_HOST"
        SET_VALUES="$SET_VALUES --set ingress.annotations.\"cert-manager\\.io/cluster-issuer\"=letsencrypt-prod"
        SET_VALUES="$SET_VALUES --set ingress.annotations.\"kubernetes\\.io/tls-acme\"=true"
        SET_VALUES="$SET_VALUES --set ingress.annotations.\"nginx\\.ingress\\.kubernetes\\.io/ssl-redirect\"=true"
    fi
    
    SET_VALUES="$SET_VALUES --set ingress.annotations.\"nginx\\.ingress\\.kubernetes\\.io/rewrite-target\"=/"
fi

# Verificar dependências
check_helm
check_kubectl

# Verificar se o chart existe
if [ ! -d "$CHART_DIR" ]; then
    error "Chart não encontrado em: $CHART_DIR"
    exit 1
fi

# Criar namespace se não existir
if [ "$NAMESPACE" != "default" ]; then
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        log "Criando namespace: $NAMESPACE"
        kubectl create namespace "$NAMESPACE"
    else
        log "Namespace $NAMESPACE já existe"
    fi
fi

# Verificar se o values.yaml existe
VALUES_FILE="$CHART_DIR/values.yaml"
if [ ! -f "$VALUES_FILE" ]; then
    error "Arquivo de valores não encontrado: $VALUES_FILE"
    exit 1
fi

log "Usando arquivo de valores: $VALUES_FILE"

# Executar helm install/upgrade
HELM_COMMAND="helm upgrade --install $RELEASE $CHART_DIR"
HELM_COMMAND="$HELM_COMMAND --namespace $NAMESPACE"
HELM_COMMAND="$HELM_COMMAND --values $VALUES_FILE"

if [ -n "$SET_VALUES" ]; then
    HELM_COMMAND="$HELM_COMMAND $SET_VALUES"
fi

if [ -n "$DRY_RUN" ]; then
    HELM_COMMAND="$HELM_COMMAND $DRY_RUN"
fi

if [ -n "$DEBUG" ]; then
    HELM_COMMAND="$HELM_COMMAND $DEBUG"
fi

log "Executando: $HELM_COMMAND"
eval $HELM_COMMAND

if [ -z "$DRY_RUN" ]; then
    log "Aguardando pods ficarem prontos..."
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=jwt-validation-api -n "$NAMESPACE" --timeout=300s
    
    log "Verificando status da aplicação..."
    kubectl get pods -l app.kubernetes.io/name=jwt-validation-api -n "$NAMESPACE"
    
    if [ "$INGRESS_ENABLED" = "true" ]; then
        log "Verificando ingress..."
        kubectl get ingress -l app.kubernetes.io/name=jwt-validation-api -n "$NAMESPACE"
    fi
    
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${GREEN}✅ Instalação concluída com sucesso!${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    
    if [ "$INGRESS_ENABLED" = "true" ]; then
        echo "Para acessar a aplicação via Ingress:"
        if [ "$INGRESS_TLS" = "true" ]; then
            echo "https://$INGRESS_HOST"
        else
            echo "http://$INGRESS_HOST"
        fi
        echo ""
        echo "Observação: Verifique se seu DNS está configurado para apontar para o endereço do cluster"
        echo "ou adicione uma entrada no arquivo /etc/hosts para testes locais."
    fi
    
    echo "Para acessar via port-forward:"
    echo "kubectl port-forward svc/$RELEASE-jwt-validation-api 8080:80 -n $NAMESPACE"
    echo ""
    echo "Depois acesse: http://localhost:8080/actuator/health"
    echo ""
    echo "Para ver logs:"
    echo "kubectl logs -l app.kubernetes.io/name=jwt-validation-api -n $NAMESPACE"
    echo ""
    echo "Para desinstalar:"
    echo "helm uninstall $RELEASE -n $NAMESPACE"
else
    log "Dry-run executado com sucesso!"
fi 