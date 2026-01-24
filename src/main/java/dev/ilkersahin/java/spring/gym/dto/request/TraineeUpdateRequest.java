package dev.ilkersahin.java.spring.gym.dto.request;

import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import jakarta.validation.Valid;

import java.time.LocalDate;

public record TraineeUpdateRequest(
        @Valid Credentials credentials,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address
) {
}
