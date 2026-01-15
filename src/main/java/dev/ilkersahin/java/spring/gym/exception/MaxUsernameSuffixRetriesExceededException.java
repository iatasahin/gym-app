package dev.ilkersahin.java.spring.gym.exception;

public class MaxUsernameSuffixRetriesExceededException extends RuntimeException {
    public MaxUsernameSuffixRetriesExceededException(String message) {
        super(message);
    }
}
