package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.exception.UnauthorizedAccessException;
import dev.ilkersahin.java.spring.gym.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;

public abstract class BaseController {

    protected String getAuthenticatedUsername(HttpServletRequest request) {
        Object username = request.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USERNAME);
        if (username == null) {
            throw new UnauthorizedAccessException("No authenticated user");
        }
        return (String) username;
    }

    protected String getAuthenticatedRole(HttpServletRequest request) {
        Object role = request.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_ROLE);
        return role != null ? (String) role : "UNKNOWN";
    }

    protected void verifyUserAccess(HttpServletRequest request, String requestedUsername) {
        String authenticatedUsername = getAuthenticatedUsername(request);
        if (!authenticatedUsername.equals(requestedUsername)) {
            throw new UnauthorizedAccessException(
                    "User '" + authenticatedUsername + "' cannot access resources of '" + requestedUsername + "'"
            );
        }
    }
}
