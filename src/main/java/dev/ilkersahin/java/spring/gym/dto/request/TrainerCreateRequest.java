package dev.ilkersahin.java.spring.gym.dto.request;

import dev.ilkersahin.java.spring.gym.model.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrainerCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull TrainingType.Type specialization
) {}
