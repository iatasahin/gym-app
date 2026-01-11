package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TraineeDAO;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private TraineeDAO traineeDAO;
    private PasswordGeneratorService passwordGeneratorService;

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setPasswordGeneratorService(PasswordGeneratorService passwordGeneratorService) {
        this.passwordGeneratorService = passwordGeneratorService;
    }


    public Trainee createTrainee(Trainee trainee) {
        log.info("Creating trainee {} {}", trainee.getFirstName(), trainee.getLastName());

        trainee.setPassword(passwordGeneratorService.generate(10));

        String defaultUsername = trainee.getFirstName() + "." + trainee.getLastName();
        trainee.setUsername(defaultUsername);
        int usernameSerialSuffix = 2;

        while (true) {
            try {
                Trainee saved = traineeDAO.createTrainee(trainee);
                log.info("Trainee created with username '{}'", saved.getUsername());
                return saved;
            } catch (UsernameExistsException e) {
                log.warn("Trainee with Username '{}' already exists — trying '{}{}'",
                        trainee.getUsername(), defaultUsername, usernameSerialSuffix
                );
                trainee.setUsername(defaultUsername + usernameSerialSuffix);
                usernameSerialSuffix++;
            }
        }
    }

    public Trainee updateTrainee(Trainee trainee) {
        log.info("Updating trainee '{}'", trainee.getUsername());
        return traineeDAO.updateTrainee(trainee);
    }

    public Optional<Trainee> deleteTrainee(String traineeUsername) {
        log.info("Deleting trainee '{}'", traineeUsername);
        return traineeDAO.deleteTrainee(traineeUsername);
    }

    public Optional<Trainee> getTrainee(String username) {
        log.debug("Fetching trainee '{}'", username);
        return traineeDAO.getTrainee(username);
    }

    public List<Trainee> getAllTrainees() {
        log.debug("Fetching all trainees");
        return traineeDAO.getAllTrainees();
    }
}
