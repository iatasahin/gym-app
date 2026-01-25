package dev.ilkersahin.java.spring.gym.dto.request;

import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import jakarta.validation.Valid;

import java.time.LocalDate;

public record TrainingSearchRequestForTrainer(
        @Valid Credentials credentials,
        LocalDate fromDate,
        LocalDate toDate,
        String traineeUsername
) {
}
