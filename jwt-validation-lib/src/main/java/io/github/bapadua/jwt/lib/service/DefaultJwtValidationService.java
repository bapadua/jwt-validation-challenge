package io.github.bapadua.jwt.lib.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * Implementação padrão do serviço de validação JWT
 * 
 * Refatorada aplicando os princípios SOLID:
 * - Single Responsibility: Focada apenas na orquestração da validação
 * - Open/Closed: Extensível através das interfaces injetadas
 * - Liskov Substitution: Implementa corretamente a interface
 * - Interface Segregation: Usa interfaces específicas para cada responsabilidade
 * - Dependency Inversion: Depende de abstrações, não de implementações concretas
 */
@Service
@ConditionalOnMissingBean(JwtValidationService.class)
public class DefaultJwtValidationService implements JwtValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultJwtValidationService.class);
    
    private final JwtClaimsExtractor claimsExtractor;
    private final JwtClaimsValidator claimsValidator;
    
    public DefaultJwtValidationService(JwtClaimsExtractor claimsExtractor, 
                                     JwtClaimsValidator claimsValidator) {
        this.claimsExtractor = claimsExtractor;
        this.claimsValidator = claimsValidator;
    }
    
    @Override
    public boolean isValidJwt(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            logger.debug("JWT token é nulo ou vazio");
            return false;
        }
        
        try {
            logger.debug("Iniciando validação do JWT token");
            
            // Validação estrutural básica
            if (!isValidJwtStructure(jwtToken)) {
                logger.warn("JWT token possui estrutura inválida");
                return false;
            }
            
            // Extrai claims usando o extrator injetado
            Map<String, String> claims = claimsExtractor.extractClaims(jwtToken.trim());
            
            if (claims == null) {
                logger.warn("Falha ao extrair claims do JWT token");
                return false;
            }
            
            logger.debug("Claims extraídos com sucesso: {}", claims.keySet());
            
            // Validações específicas dos claims usando o validador injetado
            boolean isValid = claimsValidator.validateClaims(claims);
            if (isValid) {
                logger.info("JWT token validado com sucesso para Role: {}, Name: {}", 
                          claims.get("Role"), claims.get("Name"));
            } else {
                logger.warn("Validação de claims falhou para o JWT token");
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Erro durante a validação do JWT token: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean isValidJwtStructure(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            logger.debug("JWT token é nulo ou vazio para validação estrutural");
            return false;
        }
        
        try {
            // Validação estrutural: deve ter 3 partes separadas por ponto
            String[] parts = jwtToken.trim().split("\\.");
            boolean isValid = parts.length == 3 && 
                   !parts[0].isEmpty() && 
                   !parts[1].isEmpty() && 
                   !parts[2].isEmpty();
            
            if (!isValid) {
                logger.debug("Estrutura JWT inválida - partes: {}, tamanhos: [header:{}, payload:{}, signature:{}]", 
                           parts.length, 
                           parts.length > 0 ? parts[0].length() : 0,
                           parts.length > 1 ? parts[1].length() : 0,
                           parts.length > 2 ? parts[2].length() : 0);
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("Erro durante validação estrutural do JWT: {}", e.getMessage());
            return false;
        }
    }
} 