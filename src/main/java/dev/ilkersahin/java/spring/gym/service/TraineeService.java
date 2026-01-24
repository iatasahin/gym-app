package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TraineeDAO;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.service.util.PasswordGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.UsernameGeneratorService;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    @Setter(onMethod_ = {@Autowired})
    private TraineeDAO traineeDAO;

    @Setter(onMethod_ = {@Autowired})
    private PasswordGeneratorService passwordGeneratorService;

    @Setter(onMethod_ = {@Autowired})
    private UsernameGeneratorService usernameGeneratorService;

    public Trainee createTrainee(Trainee trainee) {
        log.info("Creating Trainee: {} {}", trainee.getFirstName(), trainee.getLastName());

        trainee.setPassword(passwordGeneratorService.generate(10));
        trainee.setUsername(usernameGeneratorService.generateUniqueUsername(trainee.getFirstName(), trainee.getLastName()));

        Trainee saved = traineeDAO.createTrainee(trainee);

        log.info("Trainee created with username '{}'", saved.getUsername());

        return saved;
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
