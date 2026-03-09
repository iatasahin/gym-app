package dev.ilkersahin.java.spring.gym.dto.view;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Training type catalog entry")
public record TrainingTypeView(
        @Schema(description = "Display name of the training type", example = "Fitness")
        String trainingType,

        @Schema(description = "Unique identifier for the training type", example = "1")
        Integer trainingTypeId
) {
}
