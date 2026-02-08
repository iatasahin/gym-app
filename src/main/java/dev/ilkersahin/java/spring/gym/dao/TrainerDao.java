package dev.ilkersahin.java.spring.gym.dao;

import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    Optional<Trainer> getTrainer(String username);
    List<Trainer> getAllTrainers();
    Trainer createTrainer(Trainer trainer);
    Trainer updateTrainer(Trainer trainer);
    List<Trainee> findAssignedTrainees(String trainerUsername);
    List<Trainer> findTrainersNotAssignedToTrainee(String traineeUsername);
    List<Trainer> findByUsernames(List<String> usernames);
}
