package dev.ilkersahin.java.spring.gym.workload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record WorkloadRequest(
        @NotBlank String trainerUsername,
        @NotBlank String trainerFirstName,
        @NotBlank String trainerLastName,
        @NotNull Boolean isActive,
        @NotNull LocalDate trainingDate,
        @Positive int trainingDuration,
        @NotNull ActionType actionType
) {
}
