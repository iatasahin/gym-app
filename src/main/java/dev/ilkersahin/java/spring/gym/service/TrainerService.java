package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TrainerDAO;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.service.util.UserCreationService;
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
    private UserCreationService userCreationService;

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Autowired
    public void setUserCreationService(UserCreationService userCreationService) {
        this.userCreationService = userCreationService;
    }

    public Trainer createTrainer(Trainer trainer) {
        log.debug("TrainerService::createTrainer delegating to userCreationService::crateUser");
        return userCreationService.createUser(trainer, trainerDAO::createTrainer, "Trainer");
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
