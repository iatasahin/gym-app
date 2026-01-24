package dev.ilkersahin.java.spring.gym.dao;

import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    Optional<Trainee> getTrainee(String username);
    List<Trainee> getAllTrainees();
    Trainee createTrainee(Trainee trainee);
    Trainee updateTrainee(Trainee trainee);
    Optional<Trainee> deleteTrainee(String traineeUsername);
    List<Trainer> findAssignedTrainers(String traineeUsername);
    void updateTrainers(String traineeUsername, List<Trainer> trainers);
}
