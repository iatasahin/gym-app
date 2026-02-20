package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.exception.UnauthorizedAccessException;
import dev.ilkersahin.java.spring.gym.security.Role;
import dev.ilkersahin.java.spring.gym.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public abstract class BaseController {

    protected String getAuthenticatedUsername() {
        Optional<String> username = SecurityUtils.getCurrentUsername();
        if (username.isEmpty()) {
            throw new UnauthorizedAccessException("No authenticated user");
        }
        return username.get();
    }

    protected Optional<Role> getAuthenticatedRole() {
        return SecurityUtils.getCurrentRole();
    }

    protected void verifyUserAccess(String requestedUsername) {
        String authenticatedUsername = getAuthenticatedUsername();
        if (!authenticatedUsername.equals(requestedUsername)) {
            log.warn("User '{}' is trying and failing to access resources of '{}'",
                    authenticatedUsername, requestedUsername);
            throw new UnauthorizedAccessException(
                    "User '" + authenticatedUsername + "' cannot access resources of '" + requestedUsername + "'"
            );
        }
        log.debug("User '{}' is accessing its own resources", authenticatedUsername);
    }
}
