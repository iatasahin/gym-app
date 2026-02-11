package dev.ilkersahin.java.spring.gym.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TrainingSearchRequestForTrainer(
        @NotBlank String trainerUsername,
        LocalDate fromDate,
        LocalDate toDate,
        String traineeUsername
) {
}
