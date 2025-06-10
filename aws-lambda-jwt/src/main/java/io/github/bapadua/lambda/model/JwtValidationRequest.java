package io.github.bapadua.lambda.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Modelo de request para validação JWT no Lambda
 */
public class JwtValidationRequest {
    
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("source")
    private String source = "body"; // body, header, query
    
    @JsonProperty("headerName")
    private String headerName = "Authorization";
    
    @JsonProperty("queryParam")
    private String queryParam = "token";
    
    // Construtores
    public JwtValidationRequest() {}
    
    public JwtValidationRequest(String token) {
        this.token = token;
    }
    
    public JwtValidationRequest(String token, String source) {
        this.token = token;
        this.source = source;
    }
    
    // Getters e Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getHeaderName() {
        return headerName;
    }
    
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
    
    public String getQueryParam() {
        return queryParam;
    }
    
    public void setQueryParam(String queryParam) {
        this.queryParam = queryParam;
    }
    
    @Override
    public String toString() {
        return "JwtValidationRequest{" +
                "token='" + (token != null ? "[MASKED]" : null) + '\'' +
                ", source='" + source + '\'' +
                ", headerName='" + headerName + '\'' +
                ", queryParam='" + queryParam + '\'' +
                '}';
    }
} 