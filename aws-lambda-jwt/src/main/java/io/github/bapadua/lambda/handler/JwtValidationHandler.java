package io.github.bapadua.lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bapadua.lambda.model.JwtValidationRequest;
import io.github.bapadua.lambda.model.JwtValidationResponse;
import io.github.bapadua.lambda.service.JwtLambdaService;

/**
 * Handler principal para validação JWT em Lambda (invocação direta)
 */
public class JwtValidationHandler implements RequestHandler<JwtValidationRequest, JwtValidationResponse> {
    
    private final JwtLambdaService jwtLambdaService;
    private final ObjectMapper objectMapper;
    
    public JwtValidationHandler() {
        this.jwtLambdaService = new JwtLambdaService();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public JwtValidationResponse handleRequest(JwtValidationRequest input, Context context) {
        // Log de entrada
        context.getLogger().log("Recebido request para validação JWT: " + input);
        
        try {
            // Valida o request
            JwtValidationResponse response = jwtLambdaService.validateFromRequest(input);
            
            // Log de saída
            context.getLogger().log("Response gerado: " + response);
            
            return response;
            
        } catch (Exception e) {
            context.getLogger().log("Erro durante processamento: " + e.getMessage());
            return JwtValidationResponse.error("Erro interno: " + e.getMessage());
        }
    }
    
    /**
     * Método para validação a partir de String JSON
     */
    public JwtValidationResponse handleJsonRequest(String jsonInput, Context context) {
        try {
            JwtValidationRequest request = objectMapper.readValue(jsonInput, JwtValidationRequest.class);
            return handleRequest(request, context);
        } catch (Exception e) {
            context.getLogger().log("Erro ao parsear JSON: " + e.getMessage());
            return JwtValidationResponse.error("Erro ao processar JSON: " + e.getMessage());
        }
    }
} 