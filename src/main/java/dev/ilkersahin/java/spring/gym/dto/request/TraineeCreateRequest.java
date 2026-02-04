package dev.ilkersahin.java.spring.gym.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TraineeCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        LocalDate dateOfBirth,
        String address
) {}
