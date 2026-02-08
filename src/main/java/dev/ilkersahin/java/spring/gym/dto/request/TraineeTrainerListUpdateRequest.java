package dev.ilkersahin.java.spring.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TraineeTrainerListUpdateRequest(
        @NotBlank String traineeUsername,
        @NotEmpty List<String> trainerUsernames
) {
}
