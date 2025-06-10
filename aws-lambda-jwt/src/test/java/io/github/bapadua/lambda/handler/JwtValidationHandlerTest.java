package io.github.bapadua.lambda.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import io.github.bapadua.lambda.model.JwtValidationRequest;
import io.github.bapadua.lambda.model.JwtValidationResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para o JwtValidationHandler
 */
class JwtValidationHandlerTest {
    
    private JwtValidationHandler handler;
    
    // Tokens de teste (mesmos do backend-challenge)
    private static final String VALID_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJOYW1lIjoiSm9zZSIsIlJvbGUiOiJBZG1pbiIsIlNlZWQiOiIxMyJ9.kF3aLLts6aJmD0BN5TpBr5fJOy0lzP7Ze4Vs7nyqh0o";
    private static final String INVALID_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJOYW1lIjoiSm9zZSIsIlJvbGUiOiJBZG1pbiIsIlNlZWQiOiIxNCJ9.invalid_signature";
    private static final String JWT_WITH_NUMBERS = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJOYW1lIjoiSm9zZTEyMyIsIlJvbGUiOiJBZG1pbiIsIlNlZWQiOiI3In0.signature";
    private static final String JWT_WITH_EXTRA_CLAIMS = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJOYW1lIjoiSm9zZSIsIlJvbGUiOiJBZG1pbiIsIlNlZWQiOiIxMyIsIkV4dHJhIjoiVmFsdWUifQ.signature";
    
    @BeforeEach
    void setUp() {
        handler = new JwtValidationHandler();
    }
    
    @Test
    @DisplayName("Deve validar JWT válido")
    void testValidJwt() {
        // Given
        JwtValidationRequest request = new JwtValidationRequest(VALID_JWT);
        
        // When
        JwtValidationResponse response = handler.handleRequest(request, new MockContext());
        
        // Then
        assertNotNull(response);
        assertTrue(response.isValid());
        assertNotNull(response.getMessage());
        assertNotNull(response.getTimestamp());
    }
    
    @Test
    @DisplayName("Deve rejeitar JWT inválido")
    void testInvalidJwt() {
        // Given
        JwtValidationRequest request = new JwtValidationRequest(INVALID_JWT);
        
        // When
        JwtValidationResponse response = handler.handleRequest(request, new MockContext());
        
        // Then
        assertNotNull(response);
        assertFalse(response.isValid());
        assertNotNull(response.getMessage());
    }
    
    @Test
    @DisplayName("Deve rejeitar JWT com números no Name")
    void testJwtWithNumbersInName() {
        // Given
        JwtValidationRequest request = new JwtValidationRequest(JWT_WITH_NUMBERS);
        
        // When
        JwtValidationResponse response = handler.handleRequest(request, new MockContext());
        
        // Then
        assertNotNull(response);
        assertFalse(response.isValid());
    }
    
    @Test
    @DisplayName("Deve rejeitar JWT com claims extras")
    void testJwtWithExtraClaims() {
        // Given
        JwtValidationRequest request = new JwtValidationRequest(JWT_WITH_EXTRA_CLAIMS);
        
        // When
        JwtValidationResponse response = handler.handleRequest(request, new MockContext());
        
        // Then
        assertNotNull(response);
        assertFalse(response.isValid());
    }
    
    @Test
    @DisplayName("Deve tratar request nulo")
    void testNullRequest() {
        // When
        JwtValidationResponse response = handler.handleRequest(null, new MockContext());
        
        // Then
        assertNotNull(response);
        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("nulo"));
    }
    
    @Test
    @DisplayName("Deve tratar token vazio")
    void testEmptyToken() {
        // Given
        JwtValidationRequest request = new JwtValidationRequest("");
        
        // When
        JwtValidationResponse response = handler.handleRequest(request, new MockContext());
        
        // Then
        assertNotNull(response);
        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("não informado"));
    }
    
    @Test
    @DisplayName("Deve remover prefixo Bearer")
    void testBearerPrefix() {
        // Given
        JwtValidationRequest request = new JwtValidationRequest("Bearer " + VALID_JWT);
        
        // When
        JwtValidationResponse response = handler.handleRequest(request, new MockContext());
        
        // Then
        assertNotNull(response);
        assertTrue(response.isValid());
    }
    
    @Test
    @DisplayName("Deve processar JSON válido")
    void testJsonRequest() {
        // Given
        String jsonInput = "{\"token\":\"" + VALID_JWT + "\",\"source\":\"body\"}";
        
        // When
        JwtValidationResponse response = handler.handleJsonRequest(jsonInput, new MockContext());
        
        // Then
        assertNotNull(response);
        assertTrue(response.isValid());
    }
    
    @Test
    @DisplayName("Deve tratar JSON inválido")
    void testInvalidJson() {
        // Given
        String invalidJson = "{invalid json}";
        
        // When
        JwtValidationResponse response = handler.handleJsonRequest(invalidJson, new MockContext());
        
        // Then
        assertNotNull(response);
        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("JSON"));
    }
    
    // Mock simples para Context
    private static class MockContext implements com.amazonaws.services.lambda.runtime.Context {
        @Override public String getAwsRequestId() { return "test-request-id"; }
        @Override public String getLogGroupName() { return "test-log-group"; }
        @Override public String getLogStreamName() { return "test-log-stream"; }
        @Override public String getFunctionName() { return "test-function"; }
        @Override public String getFunctionVersion() { return "1.0"; }
        @Override public String getInvokedFunctionArn() { return "test-arn"; }
        @Override public com.amazonaws.services.lambda.runtime.CognitoIdentity getIdentity() { return null; }
        @Override public com.amazonaws.services.lambda.runtime.ClientContext getClientContext() { return null; }
        @Override public int getRemainingTimeInMillis() { return 30000; }
        @Override public int getMemoryLimitInMB() { return 512; }
        @Override public com.amazonaws.services.lambda.runtime.LambdaLogger getLogger() {
            return new com.amazonaws.services.lambda.runtime.LambdaLogger() {
                @Override public void log(String message) { System.out.println("MOCK LOG: " + message); }
                @Override public void log(byte[] message) { System.out.println("MOCK LOG: " + new String(message)); }
            };
        }
    }
} 