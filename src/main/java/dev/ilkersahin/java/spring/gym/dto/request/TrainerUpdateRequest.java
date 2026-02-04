package dev.ilkersahin.java.spring.gym.dto.request;

import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import jakarta.validation.Valid;

public record TrainerUpdateRequest(
        @Valid Credentials credentials,
        String firstName,
        String lastName,
        TrainingType.Type specialization
) {
}
