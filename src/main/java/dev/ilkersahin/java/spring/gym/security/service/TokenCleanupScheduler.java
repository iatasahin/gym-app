package dev.ilkersahin.java.spring.gym.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job to clean up expired blacklisted tokens and old login attempts.
 */
@Component
@ConditionalOnProperty(
        name = "gymapp.security.cleanup.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Clean up expired blacklisted tokens daily at 3 AM.
     */
    @Scheduled(cron = "${gymapp.security.cleanup.cron:0 0 3 * * ?}")
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired blacklisted tokens");

        try {
            int deletedTokens = tokenBlacklistService.cleanupExpiredTokens();
            log.info("Cleanup completed. Removed {} expired blacklisted tokens", deletedTokens);
        } catch (Exception e) {
            log.error("Error during token cleanup", e);
        }
    }

}
