package dev.ilkersahin.java.spring.gym.exception;

public class UserAlreadyActiveException extends RuntimeException {
    public UserAlreadyActiveException(String username) {
        super("User '" + username + "' is already active");
    }
}
