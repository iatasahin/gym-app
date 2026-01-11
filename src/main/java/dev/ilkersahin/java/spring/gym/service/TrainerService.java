package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TrainerDAO;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

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
        log.info("Creating trainer {} {}", trainer.getFirstName(), trainer.getLastName());

        trainer.setPassword(passwordGeneratorService.generate(10));

        String defaultUsername = trainer.getFirstName() + "." + trainer.getLastName();
        trainer.setUsername(defaultUsername);
        int usernameSerialSuffix = 2;

        while (true) {
            try {
                Trainer saved = trainerDAO.createTrainer(trainer);
                log.info("Trainer created with username '{}'", saved.getUsername());
                return saved;
            } catch (UsernameExistsException e) {
                log.warn("Trainer with Username '{}' already exists — trying '{}{}'",
                        trainer.getUsername(), defaultUsername, usernameSerialSuffix
                );
                trainer.setUsername(defaultUsername + usernameSerialSuffix);
                usernameSerialSuffix++;
            }
        }
    }

    public Trainer updateTrainer(Trainer trainer) {
        log.info("Updating trainer '{}'", trainer.getUsername());
        return trainerDAO.updateTrainer(trainer);
    }

    public Optional<Trainer> getTrainer(String username) {
        log.debug("Fetching trainer '{}'", username);
        return trainerDAO.getTrainer(username);
    }

    public List<Trainer> getAllTrainers() {
        log.debug("Fetching all trainers");
        return trainerDAO.getAllTrainers();
    }
}
