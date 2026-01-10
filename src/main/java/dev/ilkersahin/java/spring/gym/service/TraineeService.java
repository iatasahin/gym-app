package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TraineeDAO;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeService {

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
        trainee.setPassword(passwordGeneratorService.generate(10));

        String defaultUsername = trainee.getFirstName() + "." + trainee.getLastName();
        trainee.setUsername(defaultUsername);
        int usernameSerialSuffix = 2;

        while (true) {
            try {
                trainee = traineeDAO.createTrainee(trainee);
                return trainee;
            } catch (UsernameExistsException e) {
                trainee.setUsername(defaultUsername + usernameSerialSuffix);
                usernameSerialSuffix++;
            }
        }
    }

    public Trainee updateTrainee(Trainee trainee) {
        return traineeDAO.updateTrainee(trainee);
    }

    public Optional<Trainee> deleteTrainee(String traineeUsername) {
        return traineeDAO.deleteTrainee(traineeUsername);
    }

    public Optional<Trainee> getTrainee(String userName) {
        return traineeDAO.getTrainee(userName);
    }

    public List<Trainee> getAllTrainees() {
        return traineeDAO.getAllTrainees();
    }
}
