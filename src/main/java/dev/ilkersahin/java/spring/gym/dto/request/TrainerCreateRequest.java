package dev.ilkersahin.java.spring.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrainerCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull String specialization
) {}
