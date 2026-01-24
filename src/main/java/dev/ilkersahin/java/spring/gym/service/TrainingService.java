package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;

public interface TrainingService {
    void createTraining(TrainingCreateRequest request);
}
