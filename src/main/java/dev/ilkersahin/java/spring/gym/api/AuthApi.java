package dev.ilkersahin.java.spring.gym.api;

import dev.ilkersahin.java.spring.gym.dto.auth.LoginRequest;
import dev.ilkersahin.java.spring.gym.dto.auth.LoginResponse;
import dev.ilkersahin.java.spring.gym.dto.auth.LogoutResponse;
import dev.ilkersahin.java.spring.gym.dto.response.ErrorResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication", description = "Login and token management")
@Timed(
        value = "gym.http.auth",
        description = "Authentication controller HTTP requests"
)
public interface AuthApi {
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
    ResponseEntity<LoginResponse> login(
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
            @Valid @RequestBody LoginRequest request);


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
    ResponseEntity<LogoutResponse> logout(HttpServletRequest request);
}
