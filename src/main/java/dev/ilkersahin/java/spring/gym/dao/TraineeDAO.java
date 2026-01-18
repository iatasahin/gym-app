package dev.ilkersahin.java.spring.gym.dao;

import dev.ilkersahin.java.spring.gym.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDAO {
    Optional<Trainee> getTrainee(String username);
    List<Trainee> getAllTrainees();
    Trainee createTrainee(Trainee trainee);
    Trainee updateTrainee(Trainee trainee);
    Optional<Trainee> deleteTrainee(String traineeUsername);
}
