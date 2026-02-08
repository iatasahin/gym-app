package dev.ilkersahin.java.spring.gym.exception;

public class UserAlreadyInactiveException extends RuntimeException {
    public UserAlreadyInactiveException(String username) {
        super("User '" + username + "' is already inactive");
    }
}
