package dev.ilkersahin.java.spring.gym.security.service;


import dev.ilkersahin.java.spring.gym.exception.AccountLockedException;
import dev.ilkersahin.java.spring.gym.model.util.LoginAttempt;
import dev.ilkersahin.java.spring.gym.repository.util.LoginAttemptRepository;
import dev.ilkersahin.java.spring.gym.security.config.BruteForceProtectionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Service for tracking failed login attempts and implementing brute force protection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {
    private final LoginAttemptRepository loginAttemptRepository;
    private final BruteForceProtectionProperties properties;

    /**
     * Check if the user is currently blocked due to too many failed attempts.
     * If blocked, throws AccountLockedException with retry-after seconds.
     *
     * @param username the username to check
     * @throws AccountLockedException if the account is blocked
     */
    @Transactional(readOnly = true)
    public void checkIfBlocked(String username) {
        if (!properties.isEnabled()) {
            return;
        }
        Instant windowStart = getWindowStart();
        int attempts = loginAttemptRepository.countRecentAttempts(username, windowStart);

        if (attempts >= properties.getMaxAttempts()) {
            long retryAfterSeconds = calculateRetryAfterSeconds(username);
            log.warn("User '{}' is blocked. {} failed attempts in the last {} minutes. Retry after {} seconds.",
                    username, attempts, properties.getBlockDurationMinutes(), retryAfterSeconds);
            throw new AccountLockedException(retryAfterSeconds);
        }
    }

    /**
     * Record a failed login attempt.
     *
     * @param username  the username that failed login
     * @param ipAddress the IP address of the request (optional)
     */
    @Transactional
    public void recordFailedAttempt(String username, String ipAddress) {
        if (!properties.isEnabled()) {
            return;
        }
        LoginAttempt attempt = new LoginAttempt(username, Instant.now(), ipAddress);
        loginAttemptRepository.save(attempt);

        int totalAttempts = loginAttemptRepository.countRecentAttempts(username, getWindowStart());
        log.warn("Failed login attempt for user '{}'. Total attempts in window: {}/{}",
                username, totalAttempts, properties.getMaxAttempts());
    }

    /**
     * Clear all failed login attempts for a user (on successful login).
     *
     * @param username the username to clear
     */
    @Transactional
    public void clearAttempts(String username) {
        if (!properties.isEnabled()) {
            return;
        }
        loginAttemptRepository.deleteByUsername(username);
        log.debug("Cleared login attempts for user '{}'", username);
    }

    /**
     * Get the number of remaining attempts before lockout.
     *
     * @param username the username to check
     * @return remaining attempts (0 if blocked)
     */
    @Transactional(readOnly = true)
    public int getRemainingAttempts(String username) {
        if (!properties.isEnabled()) {
            return Integer.MAX_VALUE;
        }
        int attempts = loginAttemptRepository.countRecentAttempts(username, getWindowStart());
        return Math.max(0, properties.getMaxAttempts() - attempts);
    }

    /**
     * Calculate the start of the current time window.
     */
    private Instant getWindowStart() {
        return Instant.now().minus(Duration.ofMinutes(properties.getBlockDurationMinutes()));
    }

    /**
     * Calculate how many seconds until the user can retry.
     */
    private long calculateRetryAfterSeconds(String username) {
        return loginAttemptRepository.findMostRecentAttempt(username)
                .map(attempt -> {
                    // The first attempt in the window determines when the block expires
                    Instant blockExpires = attempt.getAttemptTime()
                            .plus(Duration.ofMinutes(properties.getBlockDurationMinutes()));
                    long seconds = Duration.between(Instant.now(), blockExpires).getSeconds();
                    return Math.max(1, seconds); // At least 1 second
                })
                .orElse((long) properties.getBlockDurationMinutes() * 60);
    }
}
