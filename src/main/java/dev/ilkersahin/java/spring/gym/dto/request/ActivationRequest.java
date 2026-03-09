package dev.ilkersahin.java.spring.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "User activation/deactivation request")
public record ActivationRequest(
        @Schema(description = "Username (must match path parameter)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String username,

        @Schema(description = "Desired active status (true = activate, false = deactivate)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Boolean active
) {
}
