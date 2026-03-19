package dev.ilkersahin.java.spring.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Trainee profile update request")
public record TraineeUpdateRequest(
        @Schema(description = "Username (must match path parameter)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String username,

        @Schema(description = "First name", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String firstName,

        @Schema(description = "Last name", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String lastName,

        @Schema(description = "Account active status", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Boolean active,

        @Schema(description = "Date of birth", format = "date")
        LocalDate dateOfBirth,

        @Schema(description = "Address")
        String address
) {
}
