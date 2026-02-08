package dev.ilkersahin.java.spring.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrainerUpdateRequest(
        @NotBlank String username,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Boolean active,
        String specialization
) {
}
