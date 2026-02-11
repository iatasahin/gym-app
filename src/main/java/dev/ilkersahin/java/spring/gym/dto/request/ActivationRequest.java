package dev.ilkersahin.java.spring.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActivationRequest(
        @NotBlank String username,
        @NotNull Boolean active
) {
}
