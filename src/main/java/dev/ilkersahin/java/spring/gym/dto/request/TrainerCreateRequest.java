package dev.ilkersahin.java.spring.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Trainer registration request")
public record TrainerCreateRequest(
        @Schema(description = "Trainer's first name", example = "Jane", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String firstName,

        @Schema(description = "Trainer's last name", example = "Smith", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String lastName,

        @Schema(description = "Training specialization type", example = "Fitness",
                allowableValues = {"Fitness", "Yoga", "Zumba", "Stretching", "Resistance"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String specialization
) {}
