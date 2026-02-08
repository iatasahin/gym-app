package dev.ilkersahin.java.spring.gym.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
@Order(2)
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Request attribute key for storing authenticated username.
     * Controllers can access via: request.getAttribute(AUTHENTICATED_USERNAME)
     */
    public static final String AUTHENTICATED_USERNAME = "authenticatedUsername";
    public static final String AUTHENTICATED_ROLE = "authenticatedRole";

    private final JwtService jwtService;

    // Public endpoints that don't require authentication
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/training-types",
            "/health",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars"
    );


    // Endpoints that are public only for POST (registration)
    private static final Set<String> PUBLIC_POST_ENDPOINTS = Set.of(
            "/api/v1/trainees",
            "/api/v1/trainers"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Check if endpoint is public
        if (isPublicEndpoint(path, method)) {
            log.debug("Public endpoint accessed: {} {}", method, path);
            filterChain.doFilter(request, response);
            return;
        }

        // Extract Authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or invalid Authorization header for {} {}", method, path);
            sendUnauthorizedResponse(response, "Missing or invalid Authorization header");
            return;
        }

        // Extract and validate token
        String token = authHeader.substring(BEARER_PREFIX.length());
        Optional<String> usernameOpt = jwtService.validateAndGetUsername(token);

        if (usernameOpt.isEmpty()) {
            log.warn("Invalid or expired JWT for {} {}", method, path);
            sendUnauthorizedResponse(response, "Invalid or expired token");
            return;
        }

        // Token is valid - set username in request attributes for controllers
        String username = usernameOpt.get();
        request.setAttribute(AUTHENTICATED_USERNAME, username);

        jwtService.getRole(token).ifPresent(role ->
                request.setAttribute(AUTHENTICATED_ROLE, role)
        );

        log.debug("Authenticated user '{}' accessing {} {}", username, method, path);

        filterChain.doFilter(request, response);
    }


    private boolean isPublicEndpoint(String path, String method) {
        // Check fully public endpoints
        for (String publicPath : PUBLIC_ENDPOINTS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }

        // Check POST-only public endpoints (registration)
        if ("POST".equalsIgnoreCase(method)) {
            for (String publicPostPath : PUBLIC_POST_ENDPOINTS) {
                if (path.equals(publicPostPath)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"error":"Unauthorized","message":"%s","status":401}
                """.formatted(message)
        );
    }
}
