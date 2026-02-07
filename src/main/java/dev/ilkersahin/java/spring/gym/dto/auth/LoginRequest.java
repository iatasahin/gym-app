package dev.ilkersahin.java.spring.gym.dto.auth;

public record LoginRequest(
        String username,
        String password
) {
}
