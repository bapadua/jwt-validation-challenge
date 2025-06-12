package io.github.bapadua.lambda.service;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bapadua.jwt.lib.service.DefaultJwtValidationService;
import io.github.bapadua.jwt.lib.service.JwtValidationService;
import io.github.bapadua.jwt.lib.service.impl.DefaultJwtClaimsExtractor;
import io.github.bapadua.jwt.lib.service.impl.DefaultJwtClaimsValidator;
import io.github.bapadua.jwt.lib.service.impl.DefaultPrimeNumberValidator;
import io.github.bapadua.lambda.model.JwtValidationRequest;
import io.github.bapadua.lambda.model.JwtValidationResponse;

/**
 * Serviço responsável pela lógica de validação JWT no ambiente Lambda
 */
public class JwtLambdaService {
    
    private final JwtValidationService jwtValidationService;
    
    public JwtLambdaService() {
        // Cria as dependências necessárias para o DefaultJwtValidationService
        DefaultPrimeNumberValidator primeValidator = new DefaultPrimeNumberValidator();
        DefaultJwtClaimsExtractor claimsExtractor = new DefaultJwtClaimsExtractor();
        DefaultJwtClaimsValidator claimsValidator = new DefaultJwtClaimsValidator(primeValidator);
        
        // Inicializa o serviço de validação com as dependências
        this.jwtValidationService = new DefaultJwtValidationService(claimsExtractor, claimsValidator);
    }
    
    /**
     * Valida JWT a partir de um request direto
     */
    public JwtValidationResponse validateFromRequest(JwtValidationRequest request) {
        try {
            if (request == null) {
                return JwtValidationResponse.error("Request não pode ser nulo");
            }
            
            String token = request.getToken();
            if (token == null || token.trim().isEmpty()) {
                return JwtValidationResponse.failure("Token não informado");
            }
            
            // Remove prefixo "Bearer " se presente
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            boolean isValid = jwtValidationService.isValidJwt(token);
            
            if (isValid) {
                return JwtValidationResponse.success("JWT válido - Todas as validações passaram");
            } else {
                return JwtValidationResponse.failure("JWT inválido - Falhou em uma ou mais validações");
            }
            
        } catch (Exception e) {
            return JwtValidationResponse.error("Erro durante validação: " + e.getMessage());
        }
    }
    
    /**
     * Valida JWT a partir de um evento do API Gateway - Retorna apenas boolean
     */
    public boolean validateJwtFromApiGateway(APIGatewayProxyRequestEvent event) {
        try {
            String token = extractTokenFromApiGatewayEvent(event);
            
            if (token == null || token.trim().isEmpty()) {
                return false;
            }
            
            // Remove prefixo "Bearer " se presente
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            return jwtValidationService.isValidJwt(token);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Valida JWT a partir de um evento do API Gateway
     */
    public JwtValidationResponse validateFromApiGateway(APIGatewayProxyRequestEvent event) {
        try {
            String token = extractTokenFromApiGatewayEvent(event);
            
            if (token == null || token.trim().isEmpty()) {
                return JwtValidationResponse.failure("Token não encontrado no request");
            }
            
            JwtValidationRequest request = new JwtValidationRequest(token);
            return validateFromRequest(request);
            
        } catch (Exception e) {
            return JwtValidationResponse.error("Erro ao processar evento do API Gateway: " + e.getMessage());
        }
    }
    
    /**
     * Extrai o token JWT do evento do API Gateway
     * Procura em diferentes locais: headers, query parameters, path parameters e body
     */
    private String extractTokenFromApiGatewayEvent(APIGatewayProxyRequestEvent event) {
        // 1. Procura no header Authorization
        Map<String, String> headers = event.getHeaders();
        if (headers != null) {
            String authHeader = headers.get("Authorization");
            if (authHeader != null && !authHeader.trim().isEmpty()) {
                return authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            }
            
            // Procura em outros headers possíveis
            String tokenHeader = headers.get("X-Token");
            if (tokenHeader != null && !tokenHeader.trim().isEmpty()) {
                return tokenHeader;
            }
        }
        
        // 2. Procura nos query parameters
        Map<String, String> queryParams = event.getQueryStringParameters();
        if (queryParams != null) {
            String tokenParam = queryParams.get("token");
            if (tokenParam != null && !tokenParam.trim().isEmpty()) {
                return tokenParam;
            }
        }
        
        // 3. Procura no path parameters
        Map<String, String> pathParams = event.getPathParameters();
        if (pathParams != null) {
            String tokenPath = pathParams.get("token");
            if (tokenPath != null && !tokenPath.trim().isEmpty()) {
                return tokenPath;
            }
        }
        
        // 4. Procura no body (JSON)
        String body = event.getBody();
        if (body != null && !body.trim().isEmpty()) {
            try {
                // Usa Jackson para fazer parse do JSON
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> bodyMap = objectMapper.readValue(body, Map.class);
                
                Object tokenObj = bodyMap.get("token");
                if (tokenObj != null) {
                    String token = tokenObj.toString();
                    if (!token.trim().isEmpty()) {
                        return token.trim();
                    }
                }
            } catch (Exception e) {
                // Se falhar no parse do JSON, tenta com parser simples como fallback
                try {
                    if (body.contains("\"token\"")) {
                        // Busca por "token":"valor" ou "token": "valor"
                        String[] parts = body.split("\"token\"\\s*:\\s*\"");
                        if (parts.length > 1) {
                            String tokenPart = parts[1];
                            int endIndex = tokenPart.indexOf("\"");
                            if (endIndex > 0) {
                                return tokenPart.substring(0, endIndex);
                            }
                        }
                    }
                } catch (Exception fallbackException) {
                    // Se ambos falharem, continua sem o token do body
                }
            }
        }
        
        // 5. Se não encontrou em lugar nenhum, retorna null
        return null;
    }
    
    /**
     * Valida múltiplos tokens de uma vez
     */
    public JwtValidationResponse validateMultipleTokens(String... tokens) {
        try {
            if (tokens == null || tokens.length == 0) {
                return JwtValidationResponse.failure("Nenhum token fornecido");
            }
            
            int validCount = 0;
            int totalCount = tokens.length;
            
            for (String token : tokens) {
                if (token != null && !token.trim().isEmpty()) {
                    if (token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }
                    
                    if (jwtValidationService.isValidJwt(token)) {
                        validCount++;
                    }
                }
            }
            
            if (validCount == totalCount) {
                return JwtValidationResponse.success(
                    String.format("Todos os %d tokens são válidos", totalCount)
                );
            } else {
                return JwtValidationResponse.failure(
                    String.format("Apenas %d de %d tokens são válidos", validCount, totalCount)
                );
            }
            
        } catch (Exception e) {
            return JwtValidationResponse.error("Erro na validação múltipla: " + e.getMessage());
        }
    }
} 