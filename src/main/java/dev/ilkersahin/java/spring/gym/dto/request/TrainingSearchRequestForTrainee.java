package dev.ilkersahin.java.spring.gym.dto.request;

import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import jakarta.validation.Valid;

import java.time.LocalDate;

public record TrainingSearchRequestForTrainee(
        @Valid Credentials credentials,
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        TrainingType.Type trainingType
) {
}
