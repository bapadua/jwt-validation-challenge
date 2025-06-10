package io.github.bapadua.jwt.lib.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuração para a biblioteca de validação JWT
 * 
 * Esta classe configura automaticamente:
 * - JwtValidationAspect (via @ComponentScan)
 * - DefaultJwtValidationService e suas dependências (via @ComponentScan + @Service/@Component)
 * 
 * Todas as implementações padrão são registradas automaticamente como beans
 * e podem ser substituídas por implementações customizadas usando @Primary ou @ConditionalOnMissingBean
 */
@AutoConfiguration
@ComponentScan(basePackages = "io.github.bapadua.jwt.lib")
public class JwtValidationAutoConfiguration {
    
    // Não é mais necessário definir beans manualmente
    // O Spring Boot encontra automaticamente as classes anotadas com @Service e @Component
    // através do @ComponentScan
} 