package dev.ilkersahin.java.spring.gym.dto.view;

import java.time.LocalDate;
import java.util.List;

public record TraineeWithListView(
        String username,
        String firstName,
        String lastName,
        boolean active,
        LocalDate dateOfBirth,
        String address,
        List<TrainerInfo> trainers
) {
}
