package dev.ilkersahin.java.spring.gym.dao;

import dev.ilkersahin.java.spring.gym.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDAO {
    Optional<Trainer> getTrainer(String username);
    List<Trainer> getAllTrainers();
    Trainer createTrainer(Trainer trainer);
    Trainer updateTrainer(Trainer trainer);
}
