package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.exception.UnauthorizedAccessException;
import dev.ilkersahin.java.spring.gym.security.AuthContextHolder;
import dev.ilkersahin.java.spring.gym.security.Role;

import java.util.Optional;

public abstract class BaseController {

    protected String getAuthenticatedUsername() {
        Optional<String> username = AuthContextHolder.getAuthenticatedUsername();
        if (username.isEmpty()) {
            throw new UnauthorizedAccessException("No authenticated user");
        }
        return username.get();
    }

    protected Optional<Role> getAuthenticatedRole() {
        return AuthContextHolder.getAuthenticatedRole();
    }

    protected void verifyUserAccess(String requestedUsername) {
        String authenticatedUsername = getAuthenticatedUsername();
        if (!authenticatedUsername.equals(requestedUsername)) {
            throw new UnauthorizedAccessException(
                    "User '" + authenticatedUsername + "' cannot access resources of '" + requestedUsername + "'"
            );
        }
    }
}
