package dev.ilkersahin.java.spring.gym.dto.auth;

import dev.ilkersahin.java.spring.gym.security.Role;

public record LoginResponse(
        String token,
        String username,
        Role role
) {
}
