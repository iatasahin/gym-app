package dev.ilkersahin.java.spring.gym.dto.view;

import dev.ilkersahin.java.spring.gym.model.TrainingType;

import java.time.LocalDate;

public record TrainingView(
        String trainingName,
        LocalDate trainingDate,
        int durationMinutes,
        TrainingType.Type trainingType,
        String traineeUsername,
        String trainerUsername
) {
}
