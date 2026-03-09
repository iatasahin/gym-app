package dev.ilkersahin.java.spring.gym.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request payload")
public record LoginRequest(
        @Schema(description = "User's username", example = "John.Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "User's password", example = "mySecretPass123", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        String password
) {
}
