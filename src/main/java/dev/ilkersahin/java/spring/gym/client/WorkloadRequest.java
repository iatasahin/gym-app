package dev.ilkersahin.java.spring.gym.client;

import java.time.LocalDate;

public record WorkloadRequest(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        Boolean isActive,
        LocalDate trainingDate,
        int trainingDuration,
        ActionType actionType
) {
}
