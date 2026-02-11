package dev.ilkersahin.java.spring.gym.exception;

/**
 * Thrown when authenticated user tries to access another user's resource.
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
