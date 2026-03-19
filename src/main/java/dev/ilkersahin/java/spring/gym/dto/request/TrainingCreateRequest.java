package dev.ilkersahin.java.spring.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(description = "Training session creation request")
public record TrainingCreateRequest(
        @Schema(example = "John.Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String traineeUsername,

        @Schema(example = "Jane.Smith", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String trainerUsername,

        @Schema(description = "Descriptive name for the training session", example = "Morning Cardio Session", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String trainingName,

        @Schema(example = "Fitness", allowableValues = {"Fitness", "Yoga", "Zumba", "Stretching", "Resistance"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String trainingType,

        @Schema(example = "2024-01-20", format = "date", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        LocalDate trainingDate,

        @Schema(description = "Training duration in minutes", example = "60", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive
        int durationMinutes
) {

}

