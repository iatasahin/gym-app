package dev.ilkersahin.java.spring.gym.dto.request;

import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TrainingCreateRequest(
        @Valid Credentials credentials,
        @NotBlank String trainerUsername,
        @NotBlank String trainingName,
        @NotNull TrainingType.Type trainingType,
        @NotNull LocalDate trainingDate,
        @Positive int durationMinutes
) {

}

