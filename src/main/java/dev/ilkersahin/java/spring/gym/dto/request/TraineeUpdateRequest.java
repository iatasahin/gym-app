package dev.ilkersahin.java.spring.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TraineeUpdateRequest(
        @NotBlank String username,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Boolean active,
        LocalDate dateOfBirth,
        String address
) {
}
