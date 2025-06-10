package io.github.bapadua.jwt.lib.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.bapadua.jwt.lib.annotation.ValidateJwt;
import io.github.bapadua.jwt.lib.service.JwtValidationService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Aspect da biblioteca jwt-validation-lib que intercepta métodos anotados com @ValidateJwt
 * e realiza a validação automática do JWT.
 * 
 * Este aspect procura por uma implementação de JwtValidationService no contexto Spring
 * do projeto que usa a biblioteca.
 */
@Aspect
@Component
public class JwtValidationAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtValidationAspect.class);
    
    @Autowired
    private JwtValidationService jwtValidationService;
    
    /**
     * Intercepta métodos anotados com @ValidateJwt
     */
    @Around("@annotation(validateJwt)")
    public Object validateJwt(ProceedingJoinPoint joinPoint, ValidateJwt validateJwt) throws Throwable {
        
        try {
            Object[] args = joinPoint.getArgs();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Parameter[] parameters = method.getParameters();
            
            logger.debug("Interceptando método: {}.{}", method.getDeclaringClass().getSimpleName(), method.getName());
            
            // Verificar se o método retorna ResponseEntity<Boolean>
            boolean returnsBooleanResponseEntity = isReturnTypeResponseEntityBoolean(method);
            logger.debug("Método retorna ResponseEntity<Boolean>: {}", returnsBooleanResponseEntity);
            
            // Extrair o JWT token
            String jwtToken = extractJwtToken(args, parameters, validateJwt);
            logger.debug("Token extraído: {}", jwtToken == null ? "null" : "[PRESENTE]");
            
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                logger.debug("Token vazio ou nulo. Opcional: {}", validateJwt.optional());
                
                if (validateJwt.optional()) {
                    // Se for opcional, continua execução
                    logger.debug("Validação opcional - continuando execução sem token");
                    return joinPoint.proceed(args);
                }
                
                if (returnsBooleanResponseEntity) {
                    logger.debug("Retornando ResponseEntity.ok(false) para token vazio");
                    return ResponseEntity.ok(false);
                } else {
                    logger.warn("Token JWT não encontrado para método não-opcional");
                    throw new IllegalArgumentException("Token JWT não encontrado");
                }
            }
            
            // Validar o JWT usando o serviço implementado no projeto
            boolean isValid = jwtValidationService.isValidJwt(jwtToken);
            logger.debug("Token válido: {}", isValid);
            
            if (!isValid) {
                if (returnsBooleanResponseEntity) {
                    logger.debug("Retornando ResponseEntity.ok(false) para token inválido");
                    return ResponseEntity.ok(false);
                } else {
                    String errorMessage = validateJwt.errorMessage();
                    logger.warn("Token JWT inválido: {}", errorMessage);
                    throw new IllegalArgumentException(errorMessage);
                }
            }
            
            // Se chegou até aqui, o JWT é válido - continuar execução
            logger.debug("Token válido - continuando execução do método");
            return joinPoint.proceed(args);
            
        } catch (Throwable ex) {
            // Log detalhado da exceção para debug
            logger.error("Erro no JwtValidationAspect: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
            throw ex;
        }
    }
    
    /**
     * Intercepta execução de métodos que têm parâmetros anotados com @ValidateJwt
     */
    @Around("execution(* *(.., @io.github.bapadua.jwt.lib.annotation.ValidateJwt (*), ..))")
    public Object validateJwtParameter(ProceedingJoinPoint joinPoint) throws Throwable {
        
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        
        // Verificar se o método retorna ResponseEntity<Boolean>
        boolean returnsBooleanResponseEntity = isReturnTypeResponseEntityBoolean(method);
        
        // Extrair tokens de parâmetros anotados com @ValidateJwt
        String jwtToken = extractJwtTokenFromAnnotatedParameters(args, parameters);
        
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            if (returnsBooleanResponseEntity) {
                return ResponseEntity.ok(false);
            } else {
                throw new IllegalArgumentException("Token JWT não encontrado em parâmetro anotado");
            }
        }
        
        // Validar o JWT usando o serviço implementado no projeto
        boolean isValid = jwtValidationService.isValidJwt(jwtToken);
        
        if (!isValid) {
            if (returnsBooleanResponseEntity) {
                return ResponseEntity.ok(false);
            } else {
                throw new IllegalArgumentException("Token JWT inválido ou expirado");
            }
        }
        
        // Se chegou até aqui, o JWT é válido - continuar execução
        return joinPoint.proceed(args);
    }
    
    /**
     * Verifica se o método retorna ResponseEntity<Boolean>
     */
    private boolean isReturnTypeResponseEntityBoolean(Method method) {
        Type returnType = method.getGenericReturnType();
        
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            
            if (rawType.equals(ResponseEntity.class)) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length == 1 && typeArguments[0].equals(Boolean.class)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Extrai o token JWT com detecção automática ou configuração manual
     */
    private String extractJwtToken(Object[] args, Parameter[] parameters, ValidateJwt validateJwt) {
        
        // 1. Tentar configuração manual primeiro
        String token = tryExtractFromConfiguration(validateJwt);
        if (token != null) return token;
        
        // 2. Tentar detecção automática de anotações Spring
        token = tryExtractFromAnnotatedParameters(args, parameters);
        if (token != null) return token;
        
        // 3. Tentar extração de propriedades de objetos @RequestBody
        if (validateJwt.enableBodyFieldExtraction()) {
            token = tryExtractFromRequestBodyFields(args, parameters, validateJwt);
            if (token != null) return token;
        }
        
        // 4. Tentar headers padrão
        token = extractTokenFromStandardHeaders(validateJwt.removeBearerPrefix());
        if (token != null) return token;
        
        // 5. Fallback: qualquer argumento String
        return tryExtractFromAnyArgument(args);
    }
    
    private String tryExtractFromConfiguration(ValidateJwt validateJwt) {
        // Header específico
        if (!validateJwt.headerName().isEmpty()) {
            return extractTokenFromHeader(validateJwt.headerName(), validateJwt.removeBearerPrefix());
        }
        
        // PathVariable específica - não podemos extrair diretamente aqui
        // RequestParam específico - não podemos extrair diretamente aqui
        
        return null;
    }
    
    private String tryExtractFromAnnotatedParameters(Object[] args, Parameter[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            
            String token = tryExtractFromRequestHeader(param);
            if (token == null) token = tryExtractFromPathVariable(args[i], param);
            if (token == null) token = tryExtractFromRequestParam(args[i], param);
            
            if (token != null) return token;
        }
        
        return null;
    }
    
    private String tryExtractFromRequestHeader(Parameter param) {
        RequestHeader headerAnnotation = param.getAnnotation(RequestHeader.class);
        if (headerAnnotation != null) {
            String headerName = getHeaderName(headerAnnotation, param);
            return extractTokenFromHeader(headerName, true);
        }
        return null;
    }
    
    private String tryExtractFromPathVariable(Object arg, Parameter param) {
        PathVariable pathAnnotation = param.getAnnotation(PathVariable.class);
        if (pathAnnotation != null) {
            return extractTokenFromArgument(arg);
        }
        return null;
    }
    
    private String tryExtractFromRequestParam(Object arg, Parameter param) {
        RequestParam paramAnnotation = param.getAnnotation(RequestParam.class);
        if (paramAnnotation != null) {
            return extractTokenFromArgument(arg);
        }
        return null;
    }
    
    /**
     * Extrai tokens especificamente de parâmetros anotados com @ValidateJwt
     */
    private String extractJwtTokenFromAnnotatedParameters(Object[] args, Parameter[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            
            // Verificar se o parâmetro tem @ValidateJwt
            ValidateJwt validateJwtAnnotation = param.getAnnotation(ValidateJwt.class);
            if (validateJwtAnnotation != null) {
                String token = extractTokenFromArgument(args[i]);
                if (token != null && !token.trim().isEmpty()) {
                    
                    // Aplicar configurações específicas da anotação
                    if (validateJwtAnnotation.removeBearerPrefix() && 
                        token.toLowerCase().startsWith("bearer ")) {
                        token = token.substring(7).trim();
                    }
                    
                    return token;
                }
            }
        }
        
        return null;
    }
    
    private String getHeaderName(RequestHeader headerAnnotation, Parameter param) {
        if (!headerAnnotation.value().isEmpty()) {
            return headerAnnotation.value();
        }
        if (!headerAnnotation.name().isEmpty()) {
            return headerAnnotation.name();
        }
        return param.getName();
    }
    
    private String extractTokenFromHeader(String headerName, boolean removeBearerPrefix) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return null;
            
            HttpServletRequest request = attributes.getRequest();
            String headerValue = request.getHeader(headerName);
            
            if (headerValue == null || headerValue.trim().isEmpty()) {
                return null;
            }
            
            if (removeBearerPrefix && headerValue.toLowerCase().startsWith("bearer ")) {
                return headerValue.substring(7).trim();
            }
            
            return headerValue.trim();
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private String extractTokenFromStandardHeaders(boolean removeBearerPrefix) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return null;
            
            HttpServletRequest request = attributes.getRequest();
            
            String[] standardHeaders = {"Authorization", "X-Auth-Token", "X-JWT-Token", "X-Access-Token"};
            
            for (String headerName : standardHeaders) {
                String headerValue = request.getHeader(headerName);
                if (headerValue != null && !headerValue.trim().isEmpty()) {
                    
                    if (removeBearerPrefix && "Authorization".equals(headerName) && 
                        headerValue.toLowerCase().startsWith("bearer ")) {
                        return headerValue.substring(7).trim();
                    }
                    
                    return headerValue.trim();
                }
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private String extractTokenFromArgument(Object arg) {
        if (arg == null) return null;
        
        if (arg instanceof String string) {
            return string;
        }
        
        return null;
    }
    
    private String tryExtractFromAnyArgument(Object[] args) {
        for (Object arg : args) {
            String token = extractTokenFromArgument(arg);
            if (token != null && !token.trim().isEmpty()) {
                return token;
            }
        }
        return null;
    }
    
    /**
     * Extrai tokens de propriedades anotadas com @JwtField em objetos @RequestBody
     */
    private String tryExtractFromRequestBodyFields(Object[] args, Parameter[] parameters, ValidateJwt validateJwt) {
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            
            // Verificar se é um parâmetro @RequestBody
            RequestBody bodyAnnotation = param.getAnnotation(RequestBody.class);
            if (bodyAnnotation != null && args[i] != null) {
                String token = extractTokenFromObject(args[i], validateJwt);
                if (token != null) return token;
            }
        }
        
        return null;
    }
    
    /**
     * Extrai token de um objeto usando reflection para encontrar propriedades anotadas com @ValidateJwt
     */
    private String extractTokenFromObject(Object obj, ValidateJwt validateJwt) {
        try {
            Class<?> clazz = obj.getClass();
            
            // Se foi especificado um campo específico, procurar por ele primeiro
            if (!validateJwt.bodyField().isEmpty()) {
                String token = extractTokenFromSpecificField(obj, clazz, validateJwt.bodyField());
                if (token != null) return token;
            }
            
            // Procurar por propriedades anotadas com @ValidateJwt
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                ValidateJwt jwtFieldAnnotation = field.getAnnotation(ValidateJwt.class);
                if (jwtFieldAnnotation != null) {
                    String token = extractTokenFromValidateJwtField(obj, field, jwtFieldAnnotation);
                    if (token != null) return token;
                }
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extrai token de um campo específico por nome
     */
    private String extractTokenFromSpecificField(Object obj, Class<?> clazz, String fieldName) {
        try {
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            
            if (value instanceof String token) {
                if (token != null && !token.trim().isEmpty()) {
                    return token.trim();
                }
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extrai token de um campo anotado com @ValidateJwt
     */
    private String extractTokenFromValidateJwtField(Object obj, java.lang.reflect.Field field, ValidateJwt validateJwtAnnotation) {
        try {
            field.setAccessible(true);
            Object value = field.get(obj);
            
            if (value instanceof String token) {
                
                if (token == null || token.trim().isEmpty()) {
                    if (!validateJwtAnnotation.optional()) {
                        return null; // Campo obrigatório está vazio
                    } else {
                        return null; // Campo opcional está vazio, continuar procurando
                    }
                }
                
                token = token.trim();
                
                // Remover prefixo Bearer se configurado
                if (validateJwtAnnotation.removeBearerPrefix() && token.toLowerCase().startsWith("bearer ")) {
                    token = token.substring(7).trim();
                }
                
                return token;
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
} 