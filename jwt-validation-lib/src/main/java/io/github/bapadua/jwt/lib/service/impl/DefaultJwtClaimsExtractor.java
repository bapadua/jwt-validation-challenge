package io.github.bapadua.jwt.lib.service.impl;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.bapadua.jwt.lib.service.JwtClaimsExtractor;

/**
 * Implementação padrão do extrator de claims JWT
 * Aplica o princípio Single Responsibility
 */
@Component
public class DefaultJwtClaimsExtractor implements JwtClaimsExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultJwtClaimsExtractor.class);
    
    @Override
    public Map<String, String> extractClaims(String jwtToken) {
        try {
            String[] parts = jwtToken.split("\\.");
            if (parts.length != 3) {
                logger.debug("JWT token não possui exatamente 3 partes: {}", parts.length);
                return null;
            }
            
            // Decodifica o payload (segunda parte)
            String payload = parts[1];
            byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
            String decodedPayload = new String(decodedBytes);
            
            logger.debug("Payload decodificado com sucesso");
            
            return parseJsonClaims(decodedPayload);
            
        } catch (Exception e) {
            logger.error("Erro ao extrair claims do payload: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Parser JSON simples para extrair claims
     */
    private Map<String, String> parseJsonClaims(String json) {
        Map<String, String> claims = new HashMap<>();
        
        try {
            // Remove chaves externas
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
            }
            
            // Divide por vírgulas, mas cuidado com vírgulas dentro de strings
            String[] pairs = splitByComma(json);
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("\"", "");
                    String value = keyValue[1].trim().replaceAll("\"", "");
                    claims.put(key, value);
                }
            }
            
        } catch (Exception e) {
            logger.error("Erro ao fazer parse dos claims JSON: {}", e.getMessage());
            return null;
        }
        
        return claims;
    }
    
    /**
     * Divide string por vírgula, respeitando strings entre aspas
     */
    private String[] splitByComma(String text) {
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (char c : text.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
        
        return result.toArray(String[]::new);
    }
} 