package io.github.bapadua.jwt.lib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation para validação automática de JWT usando a biblioteca jwt-validation-lib.
 * 
 * Esta annotation intercepta métodos para validar automaticamente tokens JWT presentes em:
 * - @RequestHeader (Authorization, X-Auth-Token, etc.)
 * - @PathVariable (qualquer path variable)
 * - @RequestParam (qualquer request parameter)
 * - Propriedades de objetos @RequestBody anotadas com @JwtField
 * 
 * DETECÇÃO AUTOMÁTICA: A annotation detecta automaticamente parâmetros anotados
 * com @RequestHeader, @PathVariable, @RequestParam ou propriedades anotadas com @JwtField.
 * 
 * Exemplo de uso:
 * 
 * @ValidateJwt
 * public ResponseEntity<?> protectedEndpoint(@PathVariable String token) {
 *     // Token já foi validado automaticamente
 *     return ResponseEntity.ok("Acesso autorizado");
 * }
 * 
 * @ValidateJwt
 * public ResponseEntity<?> headerBasedAuth(@RequestHeader("Authorization") String auth) {
 *     // Header Authorization já foi validado
 *     return ResponseEntity.ok("Autenticado");
 * }
 * 
 * @ValidateJwt
 * public ResponseEntity<?> bodyBasedAuth(@RequestBody JwtRequest request) {
 *     // Token extraído automaticamente de propriedades @JwtField
 *     return ResponseEntity.ok("Autenticado");
 * }
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateJwt {
    
    /**
     * Nome específico do parâmetro que contém o JWT.
     * Se não especificado, detecta automaticamente parâmetros anotados.
     */
    String tokenParameter() default "";
    
    /**
     * Nome específico do header HTTP que contém o JWT.
     * Se não especificado, detecta automaticamente headers anotados.
     */
    String headerName() default "";
    
    /**
     * Nome específico da path variable que contém o JWT.
     * Se não especificado, detecta automaticamente path variables anotadas.
     */
    String pathVariable() default "";
    
    /**
     * Se true, remove automaticamente o prefixo "Bearer " de tokens em headers.
     * Útil para headers Authorization padrão OAuth2/JWT.
     */
    boolean removeBearerPrefix() default true;
    
    /**
     * Se true, injeta os claims validados como parâmetro adicional.
     * O método deve ter um parâmetro do tipo JwtClaims.
     */
    boolean injectClaims() default false;
    
    /**
     * Mensagem de erro personalizada para falhas de validação.
     */
    String errorMessage() default "Token JWT inválido ou expirado";
    
    /**
     * Se true, permite que o método seja executado mesmo se o token estiver ausente.
     * Útil para endpoints opcionalmente autenticados.
     */
    boolean optional() default false;
    
    /**
     * Nome específico da propriedade em objetos @RequestBody que contém o JWT.
     * Se especificado, procura por esta propriedade em todos os parâmetros @RequestBody.
     */
    String bodyField() default "";
    
    /**
     * Se true, permite extração automática de tokens de propriedades anotadas com @JwtField
     * em objetos @RequestBody.
     */
    boolean enableBodyFieldExtraction() default true;
} 