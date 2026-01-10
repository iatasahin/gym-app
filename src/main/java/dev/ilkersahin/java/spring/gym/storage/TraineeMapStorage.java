package dev.ilkersahin.java.spring.gym.storage;

import dev.ilkersahin.java.spring.gym.dao.TraineeDAO;
import dev.ilkersahin.java.spring.gym.exception.TraineeDoesNotExistException;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class TraineeMapStorage implements TraineeDAO {
    private final ConcurrentMap<String, Trainee> traineesByUsername = new ConcurrentHashMap<>();

    @Override
    public Optional<Trainee> getTrainee(String userName) {
        Trainee trainee = traineesByUsername.get(userName);
        return trainee == null ? Optional.empty() : Optional.of(trainee);
    }

    @Override
    public List<Trainee> getAllTrainees() {
        return traineesByUsername.values().stream().toList();
    }

    @Override
    public Trainee createTrainee(Trainee trainee) {
        if (traineesByUsername.putIfAbsent(trainee.getUsername(), trainee) == null) {
            return trainee;
        } else {
            throw new UsernameExistsException(
                    "Trainee with username '%s' already exists.".formatted(trainee.getUsername())
            );
        }
    }

    @Override
    public Trainee updateTrainee(Trainee trainee) {
        Trainee traineeStored = traineesByUsername.get(trainee.getUsername());
        if(traineeStored != null){
            traineeStored.setFirstName(trainee.getFirstName());
            traineeStored.setLastName(trainee.getLastName());
            traineeStored.setDateOfBirth(trainee.getDateOfBirth());
            traineeStored.setAddress(trainee.getAddress());
            traineeStored.setActive(trainee.isActive());

            return traineeStored;
        }
        throw new TraineeDoesNotExistException(
                "Trainee with Username '%s' does not exist".formatted(trainee.getUsername())
        );
    }

    @Override
    public Optional<Trainee> deleteTrainee(String traineeUsername) {
        Trainee trainee = traineesByUsername.remove(traineeUsername);
        return trainee == null ? Optional.empty() : Optional.of(trainee);
    }
}
