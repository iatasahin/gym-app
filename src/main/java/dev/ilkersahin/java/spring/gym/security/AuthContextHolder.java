package dev.ilkersahin.java.spring.gym.security;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthContextHolder {
    private static AuthContext authContext;

    AuthContextHolder(AuthContext authContext) {
        AuthContextHolder.authContext = authContext;
    }

    public static Optional<String> getAuthenticatedUsername() {
        return Optional.ofNullable(
                authContext != null && authContext.isAuthenticated() ? authContext.getUsername() : null
        );
    }
}
