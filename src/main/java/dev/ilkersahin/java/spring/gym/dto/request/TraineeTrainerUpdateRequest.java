package dev.ilkersahin.java.spring.gym.dto.request;

import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TraineeTrainerUpdateRequest(
        @Valid Credentials credentials,
        @NotEmpty List<String> trainerUsernames
) {
}
