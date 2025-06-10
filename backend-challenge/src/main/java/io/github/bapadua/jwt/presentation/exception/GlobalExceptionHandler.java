package io.github.bapadua.jwt.presentation.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handler global de exceções
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Trata erros de validação JWT (lançados pela anotação @JwtValidation)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleJwtValidationException(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
            "JWT_VALIDATION_ERROR",
            ex.getMessage(),
            HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    /**
     * Trata erros de validação de campos
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String message = "Erro de validação: " + errors.toString();
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            message,
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * Trata exceções gerais não tratadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "Erro interno do servidor: " + ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Classe para padronizar as respostas de erro
     */
    public static class ErrorResponse {
        private final String code;
        private final String message;
        private final int status;
        
        public ErrorResponse(String code, String message, int status) {
            this.code = code;
            this.message = message;
            this.status = status;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public int getStatus() {
            return status;
        }
    }
} 