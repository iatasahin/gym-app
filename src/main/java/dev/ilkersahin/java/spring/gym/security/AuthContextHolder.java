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
        return Optional.ofNullable(authContext)
                .filter(AuthContext::isAuthenticated)
                .map(AuthContext::getUsername);
    }

    public static Optional<Role> getAuthenticatedRole() {
        return Optional.ofNullable(authContext)
                .filter(AuthContext::isAuthenticated)
                .map(AuthContext::getRole);
    }
}
