package dev.ilkersahin.java.spring.gym.dto.auth;

import dev.ilkersahin.java.spring.gym.security.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login response with JWT token")
public record LoginResponse(
        @Schema(description = "JWT token - use in Authorization header as 'Bearer <token>'",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Authenticated user's username", example = "John.Doe")
        String username,

        @Schema(description = "User's role in the system", example = "TRAINEE", allowableValues = {"TRAINEE", "TRAINER"})
        Role role
) {
}
