package dev.ilkersahin.java.spring.gym.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after successful logout")
public record LogoutResponse(
        @Schema(description = "Confirmation message", example = "Successfully logged out")
        String message,

        @Schema(description = "Username of the logged out user", example = "John.Doe")
        String username
) {
    public LogoutResponse(String username) {
        this("Successfully logged out", username);
    }
}
