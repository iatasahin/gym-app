package dev.ilkersahin.java.spring.gym.dto.view;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Trainee profile with assigned trainers")
public record TraineeWithListView(
        @Schema(example = "John.Doe")
        String username,

        @Schema(example = "John")
        String firstName,

        @Schema(example = "Doe")
        String lastName,

        @Schema(example = "true")
        boolean active,

        @Schema(example = "1990-05-15", format = "date")
        LocalDate dateOfBirth,

        @Schema(example = "123 Main St, New York")
        String address,

        @Schema(description = "List of assigned trainers")
        List<TrainerInfo> trainers
) {
}
