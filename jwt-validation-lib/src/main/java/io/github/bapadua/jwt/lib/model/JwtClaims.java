package io.github.bapadua.jwt.lib.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Representa os claims de um JWT validado.
 * Esta classe é reutilizável e extensível para diferentes tipos de aplicações.
 */
public class JwtClaims {
    
    private final String subject;
    private final String issuer;
    private final Instant issuedAt;
    private final Instant expiration;
    private final Map<String, Object> customClaims;
    
    public JwtClaims(String subject, String issuer, Instant issuedAt, Instant expiration, Map<String, Object> customClaims) {
        this.subject = subject;
        this.issuer = issuer;
        this.issuedAt = issuedAt;
        this.expiration = expiration;
        this.customClaims = customClaims != null ? Map.copyOf(customClaims) : Map.of();
    }
    
    /**
     * Construtor simplificado para casos básicos
     */
    public JwtClaims(String subject, Map<String, Object> customClaims) {
        this(subject, null, null, null, customClaims);
    }
    
    /**
     * Retorna o subject (usuário) do JWT
     */
    public String getSubject() {
        return subject;
    }
    
    /**
     * Retorna o issuer (emissor) do JWT
     */
    public String getIssuer() {
        return issuer;
    }
    
    /**
     * Retorna quando o token foi emitido
     */
    public Instant getIssuedAt() {
        return issuedAt;
    }
    
    /**
     * Retorna quando o token expira
     */
    public Instant getExpiration() {
        return expiration;
    }
    
    /**
     * Retorna todos os claims customizados
     */
    public Map<String, Object> getCustomClaims() {
        return customClaims;
    }
    
    /**
     * Retorna um claim específico
     */
    @SuppressWarnings("unchecked")
    public <T> T getClaim(String claimName, Class<T> type) {
        Object value = customClaims.get(claimName);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Verifica se um claim existe
     */
    public boolean hasClaim(String claimName) {
        return customClaims.containsKey(claimName);
    }
    
    /**
     * Verifica se o token está expirado
     */
    public boolean isExpired() {
        return expiration != null && Instant.now().isAfter(expiration);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (o instanceof JwtClaims jwtClaims) {
            return Objects.equals(subject, jwtClaims.subject) &&
                   Objects.equals(issuer, jwtClaims.issuer) &&
                   Objects.equals(issuedAt, jwtClaims.issuedAt) &&
                   Objects.equals(expiration, jwtClaims.expiration) &&
                   Objects.equals(customClaims, jwtClaims.customClaims);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(subject, issuer, issuedAt, expiration, customClaims);
    }
    
    @Override
    public String toString() {
        return "JwtClaims{" +
                "subject='" + subject + '\'' +
                ", issuer='" + issuer + '\'' +
                ", issuedAt=" + issuedAt +
                ", expiration=" + expiration +
                ", customClaims=" + customClaims +
                '}';
    }
} 