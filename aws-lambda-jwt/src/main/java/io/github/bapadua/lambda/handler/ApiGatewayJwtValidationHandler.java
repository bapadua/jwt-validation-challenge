package io.github.bapadua.lambda.handler;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import io.github.bapadua.lambda.service.JwtLambdaService;

/**
 * Handler para validação JWT via API Gateway - Retorna apenas true/false
 */
public class ApiGatewayJwtValidationHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private final JwtLambdaService jwtLambdaService;
    
    public ApiGatewayJwtValidationHandler() {
        this.jwtLambdaService = new JwtLambdaService();
    }
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Recebido evento do API Gateway: " + input.getHttpMethod() + " " + input.getPath());
        
        try {
            // Valida o JWT e retorna apenas true/false
            boolean isValid = jwtLambdaService.validateJwtFromApiGateway(input);
            
            // Cria response do API Gateway
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(200);
            response.setBody(String.valueOf(isValid));
            
            // Headers CORS
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "text/plain");
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Token");
            response.setHeaders(headers);
            
            context.getLogger().log("Response enviado: " + isValid);
            
            return response;
            
        } catch (Exception e) {
            context.getLogger().log("Erro durante processamento: " + e.getMessage());
            
            APIGatewayProxyResponseEvent errorResponse = new APIGatewayProxyResponseEvent();
            errorResponse.setStatusCode(200); // Mesmo erro retorna 200 mas false
            errorResponse.setBody("false");
            
            // Headers CORS para erro também
            Map<String, String> errorHeaders = new HashMap<>();
            errorHeaders.put("Content-Type", "text/plain");
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