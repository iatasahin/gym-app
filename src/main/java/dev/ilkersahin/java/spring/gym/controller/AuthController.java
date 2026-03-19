package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.auth.LoginRequest;
import dev.ilkersahin.java.spring.gym.dto.auth.LoginResponse;
import dev.ilkersahin.java.spring.gym.dto.auth.LogoutResponse;
import dev.ilkersahin.java.spring.gym.dto.response.ErrorResponse;
import dev.ilkersahin.java.spring.gym.exception.InvalidCredentialsException;
import dev.ilkersahin.java.spring.gym.model.User;
import dev.ilkersahin.java.spring.gym.repository.UserRepository;
import dev.ilkersahin.java.spring.gym.security.JwtService;
import dev.ilkersahin.java.spring.gym.security.Role;
import dev.ilkersahin.java.spring.gym.security.SecurityUtils;
import dev.ilkersahin.java.spring.gym.security.service.CustomUserDetailsService;
import dev.ilkersahin.java.spring.gym.security.service.LoginAttemptService;
import dev.ilkersahin.java.spring.gym.security.service.TokenBlacklistService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Login and token management")
@Timed(
        value = "gym.http.auth",
        description = "Authentication controller HTTP requests"
)
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;
    private final HttpServletRequest httpRequest;

    @PostMapping("/login")
    @Operation(summary = "User login", description = """
            Authenticate user and receive JWT token.
            
            **Public endpoint - no authentication required.**
            
            The returned token should be used in the `Authorization` header for protected endpoints:
            ```
            Authorization: Bearer <token>
            ```
            """,
            security = {})
    @ApiResponse(
            responseCode = "200",
            description = "Login successful - JWT token returned",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponse.class),
                    examples = @ExampleObject(value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "username": "John.Doe",
                        "role": "TRAINEE"
                    }
                    """)
            ))
    @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials - username or password incorrect",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-01-15T10:30:00",
                        "status": 401,
                        "error": "Unauthorized",
                        "message": "Invalid username or password",
                        "path": "/api/v1/auth/login"
                    }
                    """)
            ))
    @ApiResponse(
            responseCode = "429",
            description = "Too many failed attempts",
            headers = @Header(
                    name = "Retry-After",
                    description = "Seconds until the account is unlocked",
                    schema = @Schema(type = "integer", example = "300")
            ),
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    public ResponseEntity<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "trainee",
                                            summary = "Trainee Login",
                                            value = """
                                                    {"username": "John.Doe", "password": "mySecretPass123"}
                                                    """

                                    ),
                                    @ExampleObject(
                                            name = "trainer",
                                            summary = "Trainer login",
                                            value = """
                                                    {"username": "Jane.Smith", "password": "trainerPass456"}
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody LoginRequest request) {

        log.info("Login attempt for user '{}'", request.username());

        loginAttemptService.checkIfBlocked(request.username());

        String clientIp = getClientIpAddress();

        User user = userRepository.findByUsername(request.username())
                .orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Failed login attempt for user '{}'", request.username());
            loginAttemptService.recordFailedAttempt(request.username(), clientIp);
            throw new InvalidCredentialsException("Invalid username or password");
        }
        Role role = userDetailsService.getPrimaryRole(user);
        String token = jwtService.generateToken(request.username(), role);
        loginAttemptService.clearAttempts(request.username());

        log.info("User '{}' logged in successfully with role '{}'", request.username(), role);
        return ResponseEntity.ok(new LoginResponse(token, request.username(), role));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = """
            Invalidate the current JWT token.
            
            **🔒 Requires authentication**
            
            After logout, the token cannot be used for any further requests.
            """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(
            responseCode = "200",
            description = "Logout successful - token invalidated",
            content = @Content(schema = @Schema(implementation = LogoutResponse.class))
    )
    @ApiResponse(responseCode = "401", description = "Not authenticated or invalid token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<LogoutResponse> logout(HttpServletRequest request) {
        // Get current authenticated username
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new InvalidCredentialsException("Not authenticated"));

        // Extract token from Authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new InvalidCredentialsException("No valid token provided");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // Blacklist the token
        boolean blacklisted = tokenBlacklistService.blacklistToken(token, username);

        if (blacklisted) {
            log.info("User '{}' logged out successfully", username);
        } else {
            log.info("User '{}' logout - token was already invalidated", username);
        }

        return ResponseEntity.ok(new LogoutResponse(username));
    }

    /**
     * Extract client IP address from request, handling proxies.
     */
    private String getClientIpAddress() {
        String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs; the first one is the client
            return xForwardedFor.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }
}
