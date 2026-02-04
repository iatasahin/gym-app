package dev.ilkersahin.java.spring.gym.dto.response;

import dev.ilkersahin.java.spring.gym.dto.view.TraineeView;

public record TraineeGetResponse(
        TraineeView trainee
) {
}
