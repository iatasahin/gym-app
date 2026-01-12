package dev.ilkersahin.java.spring.gym.exception;

public class TrainerDoesNotExistException extends RuntimeException {
    public TrainerDoesNotExistException(String message) {
        super(message);
    }
}
