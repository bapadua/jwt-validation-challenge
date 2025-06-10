package io.github.bapadua.jwt.lib.service;

import java.util.Map;

/**
 * Interface responsável por extrair claims de um JWT token
 * Aplica o princípio Single Responsibility - focado apenas na extração
 */
public interface JwtClaimsExtractor {
    
    /**
     * Extrai claims do payload do JWT token
     * 
     * @param jwtToken token JWT completo
     * @return Map com os claims extraídos ou null se houver erro
     */
    Map<String, String> extractClaims(String jwtToken);
} 