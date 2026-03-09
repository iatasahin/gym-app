package dev.ilkersahin.java.spring.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request to update trainee's trainer list")
public record TraineeTrainerListUpdateRequest(
        @Schema(description = "Trainee's username", example = "John.Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String traineeUsername,

        @Schema(description = "List of trainer usernames to assign", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty
        List<String> trainerUsernames
) {
}
