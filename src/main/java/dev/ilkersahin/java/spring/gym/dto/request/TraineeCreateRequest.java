package dev.ilkersahin.java.spring.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Schema(description = "Trainee registration request")
public record TraineeCreateRequest(
        @Schema(description = "Trainee's first name", example = "John", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String firstName,

        @Schema(description = "Trainee's last name", example = "Doe", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String lastName,

        @Schema(description = "Trainee's date of birth", example = "1990-05-15", format = "date")
        LocalDate dateOfBirth,

        @Schema(description = "Trainee's address", example = "123 Main St, New York")
        String address
) {}
