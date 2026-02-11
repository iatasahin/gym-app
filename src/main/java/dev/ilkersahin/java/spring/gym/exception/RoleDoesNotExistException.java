package dev.ilkersahin.java.spring.gym.exception;

public class RoleDoesNotExistException extends RuntimeException {
    public RoleDoesNotExistException(String message) {
        super(message);
    }
}
