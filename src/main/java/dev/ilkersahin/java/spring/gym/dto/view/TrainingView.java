package dev.ilkersahin.java.spring.gym.dto.view;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Training session view")
public record TrainingView(
        @Schema(example = "Morning Cardio Session")
        String trainingName,

        @Schema(example = "2024-01-20", format = "date")
        LocalDate trainingDate,

        @Schema(example = "60")
        int durationMinutes,

        @Schema(example = "Fitness")
        String trainingType,

        @Schema(example = "John.Doe")
        String traineeUsername,

        @Schema(example = "Jane.Smith")
        String trainerUsername
) {
}
