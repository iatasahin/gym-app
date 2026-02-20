package dev.ilkersahin.java.spring.gym.exception;

import lombok.Getter;

/**
 * Exception thrown when an account is temporarily locked due to too many failed login attempts.
 */
@Getter
public class AccountLockedException extends RuntimeException {
    /**
     * Seconds until the account is unlocked.
     */
    private final long retryAfterSeconds;

    public AccountLockedException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public AccountLockedException(long retryAfterSeconds) {
        this("Account temporarily locked due to multiple failed login attempts", retryAfterSeconds);
    }
}
