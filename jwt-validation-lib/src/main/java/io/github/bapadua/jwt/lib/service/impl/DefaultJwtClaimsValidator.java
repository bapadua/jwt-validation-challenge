package io.github.bapadua.jwt.lib.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.bapadua.jwt.lib.service.JwtClaimsValidator;
import io.github.bapadua.jwt.lib.service.PrimeNumberValidator;

/**
 * Implementação padrão do validador de claims JWT
 * Aplica os princípios Single Responsibility e Dependency Inversion
 */
@Component
public class DefaultJwtClaimsValidator implements JwtClaimsValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultJwtClaimsValidator.class);
    
    // Valores válidos para a claim Role
    private static final Set<String> VALID_ROLES = new HashSet<>(Arrays.asList("Admin", "Member", "External"));
    
    // Tamanho máximo da claim Name
    private static final int MAX_NAME_LENGTH = 256;
    
    private final PrimeNumberValidator primeNumberValidator;
    
    public DefaultJwtClaimsValidator(PrimeNumberValidator primeNumberValidator) {
        this.primeNumberValidator = primeNumberValidator;
    }
    
    /**
     * Valida as claims do JWT
     * @param claims - Map<String, String> - As claims do JWT
     * @return boolean - true se as claims são válidas, false caso contrário
     */
    @Override
    public boolean validateClaims(Map<String, String> claims) {
        // Deve conter exatamente 3 claims (Name, Role e Seed)
        if (claims.size() != 3) {
            logger.warn("JWT deve conter exatamente 3 claims, mas contém: {}", claims.size());
            return false;
        }
        
        // Deve ter as claims obrigatórias
        if (!claims.containsKey("Name") || !claims.containsKey("Role") || !claims.containsKey("Seed")) {
            logger.warn("Claims obrigatórias ausentes. Claims presentes: {}", claims.keySet());
            return false;
        }
        
        // Validar Name
        String name = claims.get("Name");
        if (!isValidName(name)) {
            logger.warn("Claim Name inválida: {}", name);
            return false;
        }
        
        // Validar Role
        String role = claims.get("Role");
        if (!isValidRole(role)) {
            logger.warn("Claim Role inválida: {}. Valores válidos: {}", role, VALID_ROLES);
            return false;
        }
        
        // Validar Seed
        String seed = claims.get("Seed");
        if (!isValidSeed(seed)) {
            logger.warn("Claim Seed inválida (deve ser um número primo): {}", seed);
            return false;
        }
        
        logger.debug("Todas as validações de claims passaram");
        return true;
    }
    
    /**
     * Valida a claim Name:
     * - Não pode conter números
     * - Tamanho máximo de 256 caracteres
     */
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Verificar tamanho máximo
        if (name.length() > MAX_NAME_LENGTH) {
            return false;
        }
        
        // Verificar se não contém números
        return !name.matches(".*\\d.*");
    }
    
    /**
     * Valida a claim Role:
     * - Deve ser um dos valores: Admin, Member, External
     */
    private boolean isValidRole(String role) {
        return role != null && VALID_ROLES.contains(role);
    }
    
    /**
     * Valida a claim Seed:
     * - Deve ser um número primo
     */
    private boolean isValidSeed(String seed) {
        if (seed == null || seed.trim().isEmpty()) {
            return false;
        }
        
        try {
            long number = Long.parseLong(seed);
            return primeNumberValidator.isPrime(number);
        } catch (NumberFormatException e) {
            return false;
        }
    }
} 