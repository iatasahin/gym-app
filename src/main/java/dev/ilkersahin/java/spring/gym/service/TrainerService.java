package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TrainerDAO;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {

    private TrainerDAO trainerDAO;
    private PasswordGeneratorService passwordGeneratorService;

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Autowired
    public void setPasswordGeneratorService(PasswordGeneratorService passwordGeneratorService) {
        this.passwordGeneratorService = passwordGeneratorService;
    }

    public Trainer createTrainer(Trainer trainer) {
        trainer.setPassword(passwordGeneratorService.generate(10));

        String defaultUsername = trainer.getFirstName() + "." + trainer.getLastName();
        trainer.setUsername(defaultUsername);
        int usernameSerialSuffix = 2;

        while (true) {
            try {
                trainer = trainerDAO.createTrainer(trainer);
                return trainer;
            } catch (UsernameExistsException e) {
                trainer.setUsername(defaultUsername + usernameSerialSuffix);
                usernameSerialSuffix++;
            }
        }
    }

    public Trainer updateTrainer(Trainer trainer) {
        return trainerDAO.updateTrainer(trainer);
    }

    public Optional<Trainer> getTrainer(String userName) {
        return trainerDAO.getTrainer(userName);
    }

    public List<Trainer> getAllTrainers() {
        return trainerDAO.getAllTrainers();
    }
}
