package io.github.bapadua.lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bapadua.lambda.model.JwtValidationResponse;
import io.github.bapadua.lambda.service.JwtLambdaService;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler para validação JWT via API Gateway
 */
public class ApiGatewayJwtValidationHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private final JwtLambdaService jwtLambdaService;
    private final ObjectMapper objectMapper;
    
    public ApiGatewayJwtValidationHandler() {
        this.jwtLambdaService = new JwtLambdaService();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Recebido evento do API Gateway: " + input.getHttpMethod() + " " + input.getPath());
        
        try {
            // Valida o JWT
            JwtValidationResponse jwtResponse = jwtLambdaService.validateFromApiGateway(input);
            
            // Cria response do API Gateway
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            
            // Define status code baseado na validação
            int statusCode = jwtResponse.isValid() ? 200 : 400;
            if (jwtResponse.getMessage() != null && jwtResponse.getMessage().startsWith("Erro:")) {
                statusCode = 500;
            }
            
            response.setStatusCode(statusCode);
            response.setBody(objectMapper.writeValueAsString(jwtResponse));
            
            // Headers CORS
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Token");
            response.setHeaders(headers);
            
            context.getLogger().log("Response enviado com status: " + statusCode);
            
            return response;
            
        } catch (Exception e) {
            context.getLogger().log("Erro durante processamento: " + e.getMessage());
            
            APIGatewayProxyResponseEvent errorResponse = new APIGatewayProxyResponseEvent();
            errorResponse.setStatusCode(500);
            
            try {
                JwtValidationResponse errorJwtResponse = JwtValidationResponse.error("Erro interno do servidor");
                errorResponse.setBody(objectMapper.writeValueAsString(errorJwtResponse));
            } catch (Exception jsonError) {
                errorResponse.setBody("{\"valid\":false,\"message\":\"Erro interno do servidor\"}");
            }
            
            // Headers CORS para erro também
            Map<String, String> errorHeaders = new HashMap<>();
            errorHeaders.put("Content-Type", "application/json");
            errorHeaders.put("Access-Control-Allow-Origin", "*");
            errorResponse.setHeaders(errorHeaders);
            
            return errorResponse;
        }
    }
    
    /**
     * Método para lidar com OPTIONS (CORS preflight)
     */
    public APIGatewayProxyResponseEvent handleOptions(Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Token");
        headers.put("Access-Control-Max-Age", "86400");
        response.setHeaders(headers);
        
        return response;
    }
} 