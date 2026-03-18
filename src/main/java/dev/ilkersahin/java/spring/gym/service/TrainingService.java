package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;

import java.util.UUID;

public interface TrainingService {
    void createTraining(TrainingCreateRequest request);
    void deleteTraining(UUID trainingId);
}
