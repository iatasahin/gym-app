package dev.ilkersahin.java.spring.gym.dto.auth;

public record LoginResponse(
        String token,
        String username,
        String role
) {
}
