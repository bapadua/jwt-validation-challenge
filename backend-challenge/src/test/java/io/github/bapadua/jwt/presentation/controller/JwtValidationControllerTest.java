package io.github.bapadua.jwt.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Testes de integração para validação JWT em todos os métodos HTTP.
 * 
 * Cenários baseados nos casos de teste do arquivo Testes.md:
 * - Caso 1: JWT válido → true
 * - Caso 2: JWT inválido → false  
 * - Caso 3: JWT com Name contendo números → false
 * - Caso 4: JWT com mais de 3 claims → false
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtValidationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    
    // Tokens dos casos de teste
    private static final String VALID_JWT = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiQWRtaW4iLCJTZWVkIjoiNzg0MSIsIk5hbWUiOiJUb25pbmhvIEFyYXVqbyJ9.QY05sIjtrcJnP533kQNk8QXcaleJ1Q01jWY_ZzIZuAg";
    private static final String INVALID_JWT = "eyJhbGciOiJzI1NiJ9.dfsdfsfryJSr2xrIjoiQWRtaW4iLCJTZWVkIjoiNzg0MSIsIk5hbrUiOaJUb25pbmhvIEFyYXVqbyJ9.QY05fsdfsIjtrcJnP533kQNk8QXcaleJ1Q01jWY_ZzIZuAg";
    private static final String JWT_WITH_NUMBER_NAME = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiRXh0ZXJuYWwiLCJTZWVkIjoiODgwMzciLCJOYW1lIjoiTTRyaWEgT2xpdmlhIn0.6YD73XWZYQSSMDf6H0i3-kylz1-TY_Yt6h1cV2Ku-Qs";
    private static final String JWT_WITH_EXTRA_CLAIMS = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiTWVtYmVyIiwiT3JnIjoiQlIiLCJTZWVkIjoiMTQ2MjciLCJOYW1lIjoiVmFsZGlyIEFyYW5oYSJ9.cmrXV_Flm5mfdpfNUVopY_I2zeJUy4EZ4i3Fea98zvY";
    
    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + "/api/jwt" + uri;
    }
    
    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
    
    // ========== GET TESTS - Caso 1: Token Válido ==========
    
    @Test
    void testGetValidateJwt_ValidToken_ShouldReturnTrue() {
        HttpHeaders headers = createAuthHeaders(VALID_JWT);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        
        ResponseEntity<Boolean> response = restTemplate.exchange(
            createURLWithPort("/validate"), 
            HttpMethod.GET, 
            entity, 
            Boolean.class);
            
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody(), "Token válido deve retornar true");
    }
    
    // ========== GET TESTS - Caso 2: Token Inválido ==========
    
    @Test
    void testGetValidateJwt_InvalidToken_ShouldReturnFalse() {
        HttpHeaders headers = createAuthHeaders(INVALID_JWT);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        
        ResponseEntity<Boolean> response = restTemplate.exchange(
            createURLWithPort("/validate"), 
            HttpMethod.GET, 
            entity, 
            Boolean.class);
            
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody(), "Token inválido deve retornar false");
    }
    
    // ========== GET TESTS - Caso 3: Name com Números ==========
    
    @Test
    void testGetValidateJwt_TokenWithNumberName_ShouldReturnFalse() {
        HttpHeaders headers = createAuthHeaders(JWT_WITH_NUMBER_NAME);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        
        ResponseEntity<Boolean> response = restTemplate.exchange(
            createURLWithPort("/validate"), 
            HttpMethod.GET, 
            entity, 
            Boolean.class);
            
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody(), "Token com Name contendo números deve retornar false");
    }
    
    // ========== GET TESTS - Caso 4: Claims Extras ==========
    
    @Test
    void testGetValidateJwt_TokenWithExtraClaims_ShouldReturnFalse() {
        HttpHeaders headers = createAuthHeaders(JWT_WITH_EXTRA_CLAIMS);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        
        ResponseEntity<Boolean> response = restTemplate.exchange(
            createURLWithPort("/validate"), 
            HttpMethod.GET, 
            entity, 
            Boolean.class);
            
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody(), "Token com mais de 3 claims deve retornar false");
    }
    
    // ========== POST TESTS - Body Validation ==========
    
    @Test
    void testPostValidateBody_ValidToken_ShouldReturnTrue() {
        JwtValidationController.JwtRequest request = new JwtValidationController.JwtRequest();
        request.setJwtToken(VALID_JWT);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JwtValidationController.JwtRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<Boolean> response = restTemplate.exchange(
            createURLWithPort("/validate-body"), 
            HttpMethod.POST, 
            entity, 
            Boolean.class);
            
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody(), "Token válido no body deve retornar true");
    }
    
    @Test
    void testPostValidateBody_TokenWithNumberName_ShouldReturnFalse() {
        JwtValidationController.JwtRequest request = new JwtValidationController.JwtRequest();
        request.setJwtToken(JWT_WITH_NUMBER_NAME);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JwtValidationController.JwtRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<Boolean> response = restTemplate.exchange(
            createURLWithPort("/validate-body"), 
            HttpMethod.POST, 
            entity, 
            Boolean.class);
            
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody(), "Token com Name contendo números no body deve retornar false");
    }
    
    // ========== PUT TESTS ==========
    
    @Test
    void testPutValidateJwt_ValidToken_ShouldReturnTrue() {
        HttpHeaders headers = createAuthHeaders(VALID_JWT);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        
        ResponseEntity<Boolean> response = restTemplate.exchange(
            createURLWithPort("/validate"), 
            HttpMethod.PUT, 
            entity, 
            Boolean.class);
            
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody(), "PUT com token válido deve retornar true");
    }
    
    @Test
    void testPutValidateJwt_TokenWithExtraClaims_ShouldReturnFalse() {
        HttpHeaders headers = createAuthHeaders(JWT_WITH_EXTRA_CLAIMS);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        
        ResponseEntity<Boolean> response = restTemplate.exchange(
            createURLWithPort("/validate"), 
            HttpMethod.PUT, 
            entity, 
            Boolean.class);
            
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody(), "PUT com token extra claims deve retornar false");
    }
    
    // ========== DELETE TESTS ==========
    
    @Test
    void testDeleteValidateOptional_NoToken_ShouldReturnTrue() {
        ResponseEntity<Boolean> response = restTemplate.exchange(
            createURLWithPort("/validate-optional"), 
            HttpMethod.DELETE, 
            null, 
            Boolean.class);
            
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody(), "DELETE opcional sem token deve retornar true");
    }
    
    @Test
    void testDeleteValidateOptional_ValidToken_ShouldReturnTrue() {
        HttpHeaders headers = createAuthHeaders(VALID_JWT);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        
        ResponseEntity<Boolean> response = restTemplate.exchange(
            createURLWithPort("/validate-optional"), 
            HttpMethod.DELETE, 
            entity, 
            Boolean.class);
            
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody(), "DELETE opcional com token válido deve retornar true");
    }
    
    // ========== EDGE CASES ==========
    
    @Test
    void testMissingToken_ShouldReturnFalse() {
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort("/validate"), 
            HttpMethod.GET, 
            null, 
            String.class);
            
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("false", response.getBody(), "Sem token deve retornar false");
    }
    
    @Test
    void testEmptyToken_ShouldReturnFalse() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer ");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort("/validate"), 
            HttpMethod.GET, 
            entity, 
            String.class);
            
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("false", response.getBody(), "Token vazio deve retornar false");
    }
} 