package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TraineeDAO;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.service.util.UserCreationService;
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
    private UserCreationService userCreationService;

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setUserCreationService(UserCreationService userCreationService) {
        this.userCreationService = userCreationService;
    }


    public Trainee createTrainee(Trainee trainee) {
        log.debug("TraineeService::createTrainee delegating to userCreationService::crateUser");
        return userCreationService.createUser(trainee, traineeDAO::createTrainee, "Trainee");
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
