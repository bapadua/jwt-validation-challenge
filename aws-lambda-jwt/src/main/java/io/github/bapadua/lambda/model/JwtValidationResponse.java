package io.github.bapadua.lambda.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Modelo de response para validação JWT no Lambda
 */
public class JwtValidationResponse {
    
    @JsonProperty("valid")
    private boolean valid;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    // Construtores
    public JwtValidationResponse() {}
    
    public JwtValidationResponse(boolean valid) {
        this.valid = valid;
        this.timestamp = java.time.Instant.now().toString();
    }
    
    public JwtValidationResponse(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
        this.timestamp = java.time.Instant.now().toString();
    }
    
    // Métodos de factory para facilitar uso
    public static JwtValidationResponse success() {
        return new JwtValidationResponse(true, "JWT válido");
    }
    
    public static JwtValidationResponse success(String message) {
        return new JwtValidationResponse(true, message);
    }
    
    public static JwtValidationResponse failure() {
        return new JwtValidationResponse(false, "JWT inválido");
    }
    
    public static JwtValidationResponse failure(String message) {
        return new JwtValidationResponse(false, message);
    }
    
    public static JwtValidationResponse error(String message) {
        return new JwtValidationResponse(false, "Erro: " + message);
    }
    
    // Getters e Setters
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "JwtValidationResponse{" +
                "valid=" + valid +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
} 