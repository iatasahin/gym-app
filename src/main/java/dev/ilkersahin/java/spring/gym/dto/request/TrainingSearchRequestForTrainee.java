package dev.ilkersahin.java.spring.gym.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TrainingSearchRequestForTrainee(
        @NotBlank String traineeUsername,
        LocalDate fromDate,
        LocalDate toDate,
        String trainerUsername,
        String trainingType
) {
}
