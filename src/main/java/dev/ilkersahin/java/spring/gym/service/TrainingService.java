package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TrainingDAO;
import dev.ilkersahin.java.spring.gym.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private TrainingDAO trainingDAO;

    @Autowired
    public void setTrainingDAO(TrainingDAO trainingDAO) {
        this.trainingDAO = trainingDAO;
    }

    public List<Training> getTraining(UUID traineeId, UUID trainerId, LocalDate trainingDate) {
        log.debug("Fetching training: trainee={}, trainer={}, date={}",
                traineeId, trainerId, trainingDate
        );
        return trainingDAO.getTraining(traineeId, trainerId, trainingDate);
    }

    public Training createTraining(Training training) {
        log.info("Creating training '{}' on {}",
                training.getTrainingName(), training.getTrainingDate()
        );
        return trainingDAO.createTraining(training);
    }

    public List<Training> getAllTrainings() {
        log.debug("Fetching all trainings");
        return trainingDAO.getAllTrainings();
    }
}
