package dev.ilkersahin.java.spring.gym.dto.view;

import java.time.LocalDate;

public record TrainingView(
        String trainingName,
        LocalDate trainingDate,
        int durationMinutes,
        String trainingType,
        String traineeUsername,
        String trainerUsername
) {
}
