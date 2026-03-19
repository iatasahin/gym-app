package dev.ilkersahin.java.spring.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Password change request")
public record PasswordChangeRequest(
        @Schema(description = "Username (must match path parameter)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String username,

        @Schema(description = "Current password", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String oldPassword,

        @Schema(description = "New password", format = "password", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String newPassword
) {
}
