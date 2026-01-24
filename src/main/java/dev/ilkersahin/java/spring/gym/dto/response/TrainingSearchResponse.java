package dev.ilkersahin.java.spring.gym.dto.response;

import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;

import java.util.List;

public record TrainingSearchResponse(
        List<TrainingView> trainings
) {
}
