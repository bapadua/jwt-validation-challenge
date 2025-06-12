# Multi-stage build para otimizar o tamanho da imagem
FROM eclipse-temurin:21-jdk-alpine AS builder

# Instalar dependências de build incluindo Maven
RUN apk add --no-cache curl maven

WORKDIR /app

# Copiar arquivos de configuração Maven do projeto pai
COPY pom.xml ./pom.xml

# Copiar arquivos de configuração Maven de todos os módulos
COPY jwt-validation-lib/pom.xml ./jwt-validation-lib/pom.xml
COPY backend-challenge/pom.xml ./backend-challenge/pom.xml
COPY aws-lambda-jwt/pom.xml ./aws-lambda-jwt/pom.xml

# Cache de dependências Maven - fazer download das dependências do projeto pai
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B -f pom.xml

# Copiar código fonte de todos os módulos necessários
COPY jwt-validation-lib/src ./jwt-validation-lib/src
COPY backend-challenge/src ./backend-challenge/src
COPY aws-lambda-jwt/src ./aws-lambda-jwt/src

# Instalar o projeto pai no repositório local Maven primeiro
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean install -N -DskipTests -B

# Build da jwt-validation-lib primeiro (install para colocar no repositório local)
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean install -pl jwt-validation-lib -DskipTests -B

# Build da aplicação backend-challenge
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -pl backend-challenge -DskipTests -B && \
    mv backend-challenge/target/*.jar app.jar

# Estágio de produção
FROM eclipse-temurin:21-jre-alpine AS runtime

# Criar usuário não-root para segurança
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Instalar dependências de runtime
RUN apk add --no-cache \
    curl \
    dumb-init \
    && rm -rf /var/cache/apk/*

WORKDIR /app

# Copiar aplicação do estágio de build
COPY --from=builder --chown=appuser:appgroup /app/app.jar ./app.jar

# Configurar usuário
USER appuser

# Definir variável de ambiente para a porta
ENV SERVER_PORT=8080

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicialização com dumb-init para handling de sinais
ENTRYPOINT ["dumb-init", "--"]
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75", \
     "-XX:+UseG1GC", \
     "-XX:+UseStringDeduplication", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-Dspring.profiles.active=docker", \
     "-jar", "app.jar"] 