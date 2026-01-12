package dev.ilkersahin.java.spring.gym.dao;

import dev.ilkersahin.java.spring.gym.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TrainingDAO {
    List<Training> getTraining(UUID traineeID, UUID trainerID, LocalDate trainingDate);
    Training createTraining(Training training);
    List<Training> getAllTrainings();
}
