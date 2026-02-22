package dev.ilkersahin.java.spring.gym.dto.auth;

public record LogoutResponse(
        String message,
        String username
) {
    public LogoutResponse(String username) {
        this("Successfully logged out", username);
    }
}
