package dev.ilkersahin.java.spring.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Trainer profile update request")
public record TrainerUpdateRequest(
        @Schema(description = "Username (must match path parameter)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String username,

        @Schema(description = "Trainer's first name", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String firstName,

        @Schema(description = "Trainer's last name", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String lastName,

        @Schema(description = "Account active status", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Boolean active,

        @Schema(description = "**Read-only** - ignored on update", accessMode = Schema.AccessMode.READ_ONLY)
        String specialization
) {
}
