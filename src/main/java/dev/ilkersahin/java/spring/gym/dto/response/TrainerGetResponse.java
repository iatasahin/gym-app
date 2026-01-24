package dev.ilkersahin.java.spring.gym.dto.response;

import dev.ilkersahin.java.spring.gym.dto.view.TrainerView;

public record TrainerGetResponse(
        TrainerView trainer
) {
}
