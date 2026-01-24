package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TrainerDAO;
import dev.ilkersahin.java.spring.gym.model.Trainer;
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
public class TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    @Setter(onMethod_ = {@Autowired})
    private TrainerDAO trainerDAO;

    @Setter(onMethod_ = {@Autowired})
    private PasswordGeneratorService passwordGeneratorService;

    @Setter(onMethod_ = {@Autowired})
    private UsernameGeneratorService usernameGeneratorService;

    public Trainer createTrainer(Trainer trainer) {
        log.info("Creating Trainer: {} {}", trainer.getFirstName(), trainer.getLastName());

        trainer.setPassword(passwordGeneratorService.generate(10));
        trainer.setUsername(usernameGeneratorService.generateUniqueUsername(trainer.getFirstName(), trainer.getLastName()));

        Trainer saved = trainerDAO.createTrainer(trainer);

        log.info("Trainer created with username '{}'", saved.getUsername());

        return saved;
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
