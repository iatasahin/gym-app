package dev.ilkersahin.java.spring.gym.security;

import dev.ilkersahin.java.spring.gym.exception.RoleDoesNotExistException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT service initialized with expiration: {} ms", expirationMs);
    }

    /**
     * Generate JWT token for authenticated user.
     *
     * @param username the authenticated username
     * @param role     user role
     * @return signed JWT token
     */
    public String generateToken(String username, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        String token = Jwts.builder()
                .subject(username)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();

        log.debug("Generated JWT for user '{}' with role '{}', expires at {}", username, role, expiry);
        return token;
    }

    /**
     * Validate token and extract username.
     *
     * @param token JWT token
     * @return username if valid, empty if invalid/expired
     */
    public Optional<String> validateAndGetUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            log.debug("JWT validated for user '{}'", username);
            return Optional.of(username);

        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            return Optional.empty();

        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extract role from token (assumes token is already validated).
     *
     * @param token JWT token
     * @return role enum value, or empty if invalid/missing
     */
    public Optional<Role> getRole(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String roleStr = claims.get("role", String.class);
            return Optional.ofNullable(Role.fromString(roleStr));
        } catch (JwtException | RoleDoesNotExistException e) {
            return Optional.empty();
        }
    }

    /**
     * Extract username without full validation (for logging purposes).
     * Do NOT use for authorization decisions.
     */
    public String extractUsernameUnsafe(String token) {
        try {
            // Parse without validation - only for logging
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                // Simple extraction - not for security purposes
                if (payload.contains("\"sub\":\"")) {
                    int start = payload.indexOf("\"sub\":\"") + 7;
                    int end = payload.indexOf("\"", start);
                    return payload.substring(start, end);
                }
            }
        } catch (Exception e) {
            // Ignore - this is best-effort for logging
        }
        return "unknown";
    }
}
