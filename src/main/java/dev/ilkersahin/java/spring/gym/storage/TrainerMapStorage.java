package dev.ilkersahin.java.spring.gym.storage;

import dev.ilkersahin.java.spring.gym.dao.TrainerDAO;
import dev.ilkersahin.java.spring.gym.exception.TrainerDoesNotExistException;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class TrainerMapStorage implements TrainerDAO {
    private final ConcurrentMap<String, Trainer> trainersByUsername = new ConcurrentHashMap<>();

    @Override
    public Optional<Trainer> getTrainer(String userName) {
        Trainer trainer = trainersByUsername.get(userName);
        return trainer == null ? Optional.empty() : Optional.of(trainer);
    }

    @Override
    public List<Trainer> getAllTrainers() {
        return trainersByUsername.values().stream().toList();
    }

    @Override
    public Trainer createTrainer(Trainer trainer) {
        if (trainersByUsername.putIfAbsent(trainer.getUserName(), trainer) == null) {
            return trainer;
        } else {
            throw new UsernameExistsException(
                    "Trainer with username '%s' already exists".formatted(trainer.getUserName())
            );
        }
    }

    @Override
    public Trainer updateTrainer(Trainer trainer) {
        Trainer trainerStored = trainersByUsername.get(trainer.getUserName());
        if (trainerStored != null) {
            trainerStored.setFirstName(trainer.getFirstName());
            trainerStored.setLastname(trainer.getLastname());
            trainerStored.setActive(trainer.isActive());

            return trainerStored;
        }
        throw new TrainerDoesNotExistException(
                "Trainer with Username '%s' does not exist".formatted(trainer.getUserName())
        );
    }
}
