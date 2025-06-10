package io.github.bapadua.jwt.lib.service;

import java.util.Map;

/**
 * Interface responsável por validar claims de um JWT
 * Aplica os princípios Single Responsibility e Open/Closed
 */
public interface JwtClaimsValidator {
    
    /**
     * Valida os claims extraídos do JWT
     * 
     * @param claims Map com os claims a serem validados
     * @return true se todos os claims são válidos, false caso contrário
     */
    boolean validateClaims(Map<String, String> claims);
} 