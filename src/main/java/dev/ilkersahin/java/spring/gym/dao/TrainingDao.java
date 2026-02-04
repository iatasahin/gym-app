package dev.ilkersahin.java.spring.gym.dao;

import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TrainingDao {
    List<Training> getTraining(UUID traineeID, UUID trainerID, LocalDate trainingDate);
    Training createTraining(Training training);
    List<Training> getAllTrainings();
    List<Training> findForTrainee(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            TrainingType.Type trainingType
    );
    List<Training> findForTrainer(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername
    );
}
