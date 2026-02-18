package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;

import java.time.LocalDate;
import java.util.List;

/**
 * Custom repository fragment for complex Training queries.
 * These methods use Criteria API for dynamic predicates.
 */
public interface TrainingRepositoryCustom {

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
