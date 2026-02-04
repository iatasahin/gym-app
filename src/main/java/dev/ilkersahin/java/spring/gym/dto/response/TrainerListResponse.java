package dev.ilkersahin.java.spring.gym.dto.response;

import dev.ilkersahin.java.spring.gym.dto.view.TrainerView;

import java.util.List;

public record TrainerListResponse(
        List<TrainerView> trainers
) {
}
