package dev.ilkersahin.java.spring.gym.security.service;

import dev.ilkersahin.java.spring.gym.model.util.BlacklistedToken;
import dev.ilkersahin.java.spring.gym.repository.util.BlacklistedTokenRepository;
import dev.ilkersahin.java.spring.gym.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Service for managing blacklisted (invalidated) JWT tokens.
 * Used for logout functionality - when a user logs out, their token is blacklisted
 * so it can't be used even if it hasn't expired yet.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtService jwtService;

    /**
     * Blacklist a token (on logout).
     *
     * @param token    the JWT token to blacklist
     * @param username the username associated with the token
     * @return true if successfully blacklisted, false if already blacklisted
     */
    @Transactional
    public boolean blacklistToken(String token, String username) {
        String tokenHash = hashToken(token);

        // Check if already blacklisted
        if (blacklistedTokenRepository.existsByTokenHash(tokenHash)) {
            log.debug("Token already blacklisted for user '{}'", username);
            return false;
        }

        // Get token expiration time
        Instant expirationTime = jwtService.getExpiration(token)
                .orElse(Instant.now().plusSeconds(jwtService.getExpirationMs() / 1000));

        try {
            BlacklistedToken blacklistedToken = new BlacklistedToken(tokenHash, expirationTime, username);
            blacklistedTokenRepository.save(blacklistedToken);
            log.info("Token blacklisted for user '{}', expires at {}", username, expirationTime);
            return true;
        } catch (DataIntegrityViolationException e) {
            // Race condition - token was blacklisted by another request
            log.debug("Token already blacklisted (concurrent request) for user '{}'", username);
            return false;
        }
    }

    /**
     * Check if a token is blacklisted.
     *
     * @param token the JWT token to check
     * @return true if the token is blacklisted
     */
    @Transactional(readOnly = true)
    public boolean isBlacklisted(String token) {
        String tokenHash = hashToken(token);
        boolean blacklisted = blacklistedTokenRepository.existsByTokenHash(tokenHash);

        if (blacklisted) {
            log.debug("Token is blacklisted");
        }

        return blacklisted;
    }

    /**
     * Clean up expired blacklisted tokens.
     * Should be called periodically by a scheduled job.
     *
     * @return number of tokens removed
     */
    @Transactional
    public int cleanupExpiredTokens() {
        int deleted = blacklistedTokenRepository.deleteExpiredTokens(Instant.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired blacklisted tokens", deleted);
        }
        return deleted;
    }

    /**
     * Get count of active (non-expired) blacklisted tokens.
     * Useful for monitoring.
     */
    @Transactional(readOnly = true)
    public long getActiveBlacklistedTokenCount() {
        return blacklistedTokenRepository.countActiveBlacklistedTokens(Instant.now());
    }

    /**
     * Hash a token using SHA-256.
     * We store the hash instead of the full token for security.
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available in Java
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

}
