package io.github.bapadua.jwt.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.bapadua.jwt.lib.annotation.ValidateJwt;

/**
 * Controller REST para validação de JWT usando a anotação @ValidateJwt
 * com IMPLEMENTAÇÃO CUSTOMIZADA (JwtValidationServiceImpl)
 * 
 * Este controller usa validação customizada que inclui:
 * - Claims obrigatórios: Name, Role, Seed
 * - Número configurável de claims
 * - Chave secreta específica
 * - Validação de tipos de dados dos claims
 * 
 * Compare com SimpleJwtController que usa comportamento padrão da biblioteca.
 */
@RestController
@RequestMapping("/api/jwt")
public class JwtValidationController {

    /**
     * Endpoint para validação JWT via header Authorization
     * A anotação @ValidateJwt faz toda a validação automaticamente
     * 
     * @param authorization header Authorization contendo o JWT
     * @return true se chegou até aqui (token foi validado pela anotação)
     */
    @GetMapping("/validate")
    @ValidateJwt(headerName = "Authorization", removeBearerPrefix = true)
    public ResponseEntity<Boolean> validateJwt(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        // Se chegou até aqui, o JWT foi validado automaticamente pela anotação
        return ResponseEntity.ok(true);
    }

    /**
     * Endpoint para validação JWT via parâmetro de query
     * 
     * @param jwt o token JWT como parâmetro
     * @return true se chegou até aqui (token foi validado pela anotação)
     */
    @GetMapping("/validate-param")
    @ValidateJwt
    public ResponseEntity<Boolean> validateJwtFromParam(@RequestParam("jwt") String jwt) {// Se chegou até aqui, o JWT
                                                                                          // foi validado
                                                                                          // automaticamente pela
                                                                                          // anotação
        return ResponseEntity.ok(true);
    }

    /**
     * Endpoint para validação JWT via path variable
     * 
     * @param token o token JWT como path variable
     * @return true se chegou até aqui (token foi validado pela anotação)
     */
    @GetMapping("/validate-path/{token}")
    @ValidateJwt
    public ResponseEntity<Boolean> validateJwtFromPath(@PathVariable("token") String token) {
        // Se chegou até aqui, o JWT foi validado automaticamente pela anotação
        return ResponseEntity.ok(true);
    }

    /**
     * Endpoint para validação JWT via header customizado
     * 
     * @param authToken o token JWT do header customizado
     * @return true se chegou até aqui (token foi validado pela anotação)
     */
    @PostMapping("/validate-custom-header")
    @ValidateJwt(headerName = "X-Auth-Token", removeBearerPrefix = false)
    public ResponseEntity<Boolean> validateJwtFromCustomHeader(@RequestHeader("X-Auth-Token") String authToken) {
        // Se chegou até aqui, o JWT foi validado automaticamente pela anotação
        return ResponseEntity.ok(true);
    }

    /**
     * Endpoint para validação JWT via corpo da requisição
     * Agora com suporte automático para extração de propriedades @ValidateJwt
     * 
     * @param request objeto contendo o token JWT em propriedade anotada
     * @return true se chegou até aqui (token foi validado pela anotação)
     */
    @PostMapping("/validate-body")
    @ValidateJwt
    public ResponseEntity<Boolean> validateJwtFromBody(@RequestBody JwtRequest request) {
        // A anotação agora detecta automaticamente o token da propriedade @ValidateJwt
        return ResponseEntity.ok(true);
    }

    /**
     * Endpoint para demonstrar validação direta em parâmetros
     * 
     * @param authToken token JWT anotado diretamente no parâmetro
     * @return true se chegou até aqui
     */
    @PostMapping("/validate-direct-header")
    public ResponseEntity<Boolean> validateDirectHeader(@ValidateJwt @RequestHeader("Authorization") String authToken) {
        // A anotação @ValidateJwt é aplicada diretamente no parâmetro
        return ResponseEntity.ok(true);
    }

    /**
     * Endpoint para demonstrar validação direta em path variable
     * 
     * @param token token JWT anotado diretamente na path variable
     * @return true se chegou até aqui
     */
    @GetMapping("/validate-direct-path/{token}")
    public ResponseEntity<Boolean> validateDirectPath(@ValidateJwt @PathVariable String token) {
        // A anotação @ValidateJwt é aplicada diretamente na path variable
        return ResponseEntity.ok(true);
    }

    /**
     * Endpoint para demonstrar validação direta em query parameter
     * 
     * @param jwt token JWT anotado diretamente no parâmetro
     * @return true se chegou até aqui
     */
    @GetMapping("/validate-direct-param")
    public ResponseEntity<Boolean> validateDirectParam(@ValidateJwt @RequestParam("jwt") String jwt) {
        // A anotação @ValidateJwt é aplicada diretamente no parâmetro
        return ResponseEntity.ok(true);
    }

    /**
     * Endpoint para demonstrar validação de campo específico do body
     * 
     * @param request objeto com multiple tokens
     * @return true se chegou até aqui
     */
    @PostMapping("/validate-body-specific")
    @ValidateJwt(bodyField = "adminToken")
    public ResponseEntity<Boolean> validateSpecificBodyField(@RequestBody MultiTokenRequest request) {
        // A anotação extrairá especificamente o campo "adminToken"
        return ResponseEntity.ok(true);
    }

    /**
     * Endpoint para demonstrar validação opcional de propriedades
     * 
     * @param request objeto que pode ou não ter token
     * @return true sempre
     */
    @PostMapping("/validate-body-optional")
    @ValidateJwt(optional = true)
    public ResponseEntity<Boolean> validateOptionalBodyField(@RequestBody OptionalTokenRequest request) {
        // Executa mesmo se não houver token nas propriedades
        return ResponseEntity.ok(true);
    }

    /**
     * Endpoint que demonstra validação opcional
     * Se não houver token, o método ainda será executado
     * 
     * @return true sempre
     */
    @GetMapping("/validate-optional")
    @ValidateJwt(optional = true)
    public ResponseEntity<Boolean> validateOptionalJwt() {
        // Este método é executado mesmo sem token JWT
        return ResponseEntity.ok(true);
    }

    /**
     * Endpoint com múltiplas fontes de token
     * A anotação detecta automaticamente de qualquer fonte anotada
     * 
     * @param headerToken token do header (opcional)
     * @param pathToken   token da URL
     * @param paramToken  token do parâmetro (opcional)
     * @return true se chegou até aqui
     */
    @GetMapping("/validate-multiple/{pathToken}")
    @ValidateJwt(errorMessage = "Token JWT é obrigatório para acessar este recurso")
    public ResponseEntity<Boolean> validateMultipleSources(
            @RequestHeader(value = "X-Auth-Token", required = false) String headerToken,
            @PathVariable String pathToken,
            @RequestParam(value = "jwt", required = false) String paramToken) {

        // A anotação detecta automaticamente na ordem: header > path > param
        return ResponseEntity.ok(true);
    }

    /**
     * Classe para receber JWT no corpo da requisição com anotação @ValidateJwt
     */
    public static class JwtRequest {
        @ValidateJwt
        private String jwtToken;

        @ValidateJwt(removeBearerPrefix = true)
        private String authToken;

        private String otherData;

        public String getJwtToken() {
            return jwtToken;
        }

        public void setJwtToken(String jwtToken) {
            this.jwtToken = jwtToken;
        }

        public String getAuthToken() {
            return authToken;
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

        public String getOtherData() {
            return otherData;
        }

        public void setOtherData(String otherData) {
            this.otherData = otherData;
        }
    }

    /**
     * Classe para demonstrar múltiplos tokens no body
     */
    public static class MultiTokenRequest {
        @ValidateJwt(optional = true)
        private String userToken;

        @ValidateJwt
        private String adminToken;

        private String data;

        // getters e setters
        public String getUserToken() {
            return userToken;
        }

        public void setUserToken(String userToken) {
            this.userToken = userToken;
        }

        public String getAdminToken() {
            return adminToken;
        }

        public void setAdminToken(String adminToken) {
            this.adminToken = adminToken;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    /**
     * Classe para demonstrar token opcional
     */
    public static class OptionalTokenRequest {
        @ValidateJwt(optional = true)
        private String optionalToken;

        private String publicData;

        // getters e setters
        public String getOptionalToken() {
            return optionalToken;
        }

        public void setOptionalToken(String optionalToken) {
            this.optionalToken = optionalToken;
        }

        public String getPublicData() {
            return publicData;
        }

        public void setPublicData(String publicData) {
            this.publicData = publicData;
        }
    }

    // ====== PUT ENDPOINTS ======

    /**
     * PUT - Validação em método (comportamento original)
     */
    @PutMapping("/validate")
    @ValidateJwt
    public ResponseEntity<Boolean> putValidateJwt(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(true);
    }

    /**
     * PUT - Validação direta em parâmetro
     */
    @PutMapping("/validate-direct-header")
    public ResponseEntity<Boolean> putValidateDirectHeader(
            @ValidateJwt @RequestHeader("Authorization") String authToken) {
        return ResponseEntity.ok(true);
    }

    /**
     * PUT - Validação via body com propriedades anotadas
     */
    @PutMapping("/validate-body")
    @ValidateJwt
    public ResponseEntity<Boolean> putValidateJwtFromBody(@RequestBody JwtRequest request) {
        return ResponseEntity.ok(true);
    }

    // ====== PATCH ENDPOINTS ======

    /**
     * PATCH - Validação em path variable
     */
    @PatchMapping("/validate-path/{token}")
    @ValidateJwt
    public ResponseEntity<Boolean> patchValidateJwtFromPath(@PathVariable("token") String token) {
        return ResponseEntity.ok(true);
    }

    /**
     * PATCH - Validação direta em parâmetro path
     */
    @PatchMapping("/validate-direct-path/{token}")
    public ResponseEntity<Boolean> patchValidateDirectPath(@ValidateJwt @PathVariable String token) {
        return ResponseEntity.ok(true);
    }

    /**
     * PATCH - Validação via query parameter
     */
    @PatchMapping("/validate-param")
    @ValidateJwt
    public ResponseEntity<Boolean> patchValidateJwtFromParam(@RequestParam("jwt") String jwt) {
        return ResponseEntity.ok(true);
    }

    // ====== DELETE ENDPOINTS ======

    /**
     * DELETE - Validação opcional em método
     */
    @DeleteMapping("/validate-optional")
    @ValidateJwt(optional = true)
    public ResponseEntity<Boolean> deleteValidateOptionalJwt() {
        return ResponseEntity.ok(true);
    }

    /**
     * DELETE - Validação direta em query parameter
     */
    @DeleteMapping("/validate-direct-param")
    public ResponseEntity<Boolean> deleteValidateDirectParam(@ValidateJwt @RequestParam("jwt") String jwt) {
        return ResponseEntity.ok(true);
    }

    /**
     * DELETE - Validação múltiplas fontes
     */
    @DeleteMapping("/validate-multiple/{pathToken}")
    @ValidateJwt(errorMessage = "Token JWT é obrigatório para exclusão")
    public ResponseEntity<Boolean> deleteValidateMultipleSources(
            @RequestHeader(value = "X-Auth-Token", required = false) String headerToken,
            @PathVariable String pathToken,
            @RequestParam(value = "jwt", required = false) String paramToken) {
        return ResponseEntity.ok(true);
    }
}