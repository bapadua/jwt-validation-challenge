package io.github.bapadua.jwt.lib.service;

/**
 * Interface de serviço para validação de JWT
 * 
 * Esta interface deve ser implementada pelos projetos que usam a biblioteca
 * para definir a lógica específica de validação de acordo com suas regras de negócio.
 */
public interface JwtValidationService {
    
    /**
     * Valida um JWT e retorna true/false
     * 
     * @param jwtToken o token JWT como string
     * @return true se válido, false caso contrário
     */
    boolean isValidJwt(String jwtToken);
    
    /**
     * Verifica se o JWT é estruturalmente válido (formato)
     * 
     * @param jwtToken o token JWT como string
     * @return true se for válido estruturalmente
     */
    boolean isValidJwtStructure(String jwtToken);
} 