package dev.ilkersahin.java.spring.gym.storage;

import dev.ilkersahin.java.spring.gym.dao.TrainerDAO;
import dev.ilkersahin.java.spring.gym.exception.TrainerDoesNotExistException;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class TrainerMapStorage implements TrainerDAO {
    private static final Logger log = LoggerFactory.getLogger(TraineeMapStorage.class);

    private final ConcurrentMap<String, Trainer> trainersByUsername = new ConcurrentHashMap<>();

    @Override
    public Optional<Trainer> getTrainer(String username) {
        Trainer trainer = trainersByUsername.get(username);
        log.debug("Lookup trainer '{}': {}", username, trainer != null ? "FOUND" : "NOT FOUND");
        return Optional.ofNullable(trainer);
    }

    @Override
    public List<Trainer> getAllTrainers() {
        log.debug("Retrieving all trainers ({} total)", trainersByUsername.size());
        return trainersByUsername.values().stream().toList();
    }

    @Override
    public Trainer createTrainer(Trainer trainer) {
        log.debug("Creating trainer '{}'", trainer.getUsername());

        if (trainersByUsername.putIfAbsent(trainer.getUsername(), trainer) == null) {
            log.info("Trainer '{}' stored successfully", trainer.getUsername());
            return trainer;
        } else {
            log.warn("Attempt to create duplicate trainer '{}'", trainer.getUsername());

            throw new UsernameExistsException(
                    "Trainer with username '%s' already exists".formatted(trainer.getUsername())
            );
        }
    }

    @Override
    public Trainer updateTrainer(Trainer trainer) {
        log.debug("Updating trainer '{}'", trainer.getUsername());

        Trainer trainerStored = trainersByUsername.get(trainer.getUsername());
        if (trainerStored != null) {
            trainerStored.setFirstName(trainer.getFirstName());
            trainerStored.setLastName(trainer.getLastName());
            trainerStored.setActive(trainer.isActive());

            log.info("Trainer '{}' updated", trainer.getUsername());
            return trainerStored;
        }

        log.error("Update failed — trainer '{}' does not exist", trainer.getUsername());
        throw new TrainerDoesNotExistException(
                "Trainer with Username '%s' does not exist".formatted(trainer.getUsername())
        );
    }
}
