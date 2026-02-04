package dev.ilkersahin.java.spring.gym.dto.request;

import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import jakarta.validation.Valid;

public record TraineeDeleteRequest(
        @Valid Credentials credentials
) {
}
