# Multi-stage build para otimizar o tamanho da imagem
FROM eclipse-temurin:21-jdk-alpine AS builder

# Instalar dependências de build
RUN apk add --no-cache curl

WORKDIR /app

# Copiar arquivos de configuração Maven
COPY pom.xml ./
COPY ../jwt-validation-lib/pom.xml ./jwt-validation-lib/
COPY ../pom.xml ./parent/

# Cache de dependências Maven
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src
COPY ../jwt-validation-lib ./jwt-validation-lib

# Build da aplicação
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B && \
    mv target/*.jar app.jar

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