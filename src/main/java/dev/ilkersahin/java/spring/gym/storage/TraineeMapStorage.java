package dev.ilkersahin.java.spring.gym.storage;

import dev.ilkersahin.java.spring.gym.dao.TraineeDAO;
import dev.ilkersahin.java.spring.gym.exception.TraineeDoesNotExistException;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class TraineeMapStorage implements TraineeDAO {
    private static final Logger log = LoggerFactory.getLogger(TraineeMapStorage.class);

    private final ConcurrentMap<String, Trainee> traineesByUsername = new ConcurrentHashMap<>();

    @Override
    public Optional<Trainee> getTrainee(String username) {
        Trainee trainee = traineesByUsername.get(username);
        log.debug("Lookup trainee '{}': {}", username, trainee != null ? "FOUND" : "NOT FOUND");
        return Optional.ofNullable(trainee);
    }

    @Override
    public List<Trainee> getAllTrainees() {
        log.debug("Retrieving all trainees ({} total)", traineesByUsername.size());
        return traineesByUsername.values().stream().toList();
    }

    @Override
    public Trainee createTrainee(Trainee trainee) {
        log.debug("Creating trainee '{}'", trainee.getUsername());

        if (traineesByUsername.putIfAbsent(trainee.getUsername(), trainee) == null) {
            log.info("Trainee '{}' stored successfully", trainee.getUsername());
            return trainee;
        } else {
            log.warn("Attempt to create duplicate trainee '{}'", trainee.getUsername());

            throw new UsernameExistsException(
                    "Trainee with username '%s' already exists.".formatted(trainee.getUsername())
            );
        }
    }

    @Override
    public Trainee updateTrainee(Trainee trainee) {
        log.debug("Updating trainee '{}'", trainee.getUsername());

        Trainee traineeStored = traineesByUsername.get(trainee.getUsername());
        if(traineeStored != null){
            traineeStored.setFirstName(trainee.getFirstName());
            traineeStored.setLastName(trainee.getLastName());
            traineeStored.setDateOfBirth(trainee.getDateOfBirth());
            traineeStored.setAddress(trainee.getAddress());
            traineeStored.setActive(trainee.isActive());

            log.info("Trainee '{}' updated", trainee.getUsername());
            return traineeStored;
        }

        log.error("Update failed — trainee '{}' does not exist", trainee.getUsername());
        throw new TraineeDoesNotExistException(
                "Trainee with Username '%s' does not exist".formatted(trainee.getUsername())
        );
    }

    @Override
    public Optional<Trainee> deleteTrainee(String traineeUsername) {
        log.debug("Deleting trainee '{}'", traineeUsername);

        Trainee trainee = traineesByUsername.remove(traineeUsername);

        if (trainee == null) {
            log.warn("Delete failed — trainee '{}' not found", traineeUsername);
        } else {
            log.info("Trainee '{}' deleted", traineeUsername);
        }

        return Optional.ofNullable(trainee);
    }
}
