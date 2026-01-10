package dev.ilkersahin.java.spring.gym.exception;

public class TraineeDoesNotExistException extends RuntimeException {
    public TraineeDoesNotExistException(String message) {
        super(message);
    }
}
