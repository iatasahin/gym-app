package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TrainingDAO;
import dev.ilkersahin.java.spring.gym.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TrainingService {
    private TrainingDAO trainingDAO;

    @Autowired
    public void setTrainingDAO(TrainingDAO trainingDAO) {
        this.trainingDAO = trainingDAO;
    }

    public List<Training> getTraining(UUID traineeId, UUID trainerId, LocalDate trainingDate) {
        return trainingDAO.getTraining(traineeId, trainerId, trainingDate);
    }

    public Training createTraining(Training training) {
        return trainingDAO.createTraining(training);
    }

    public List<Training> getAllTrainings() {
        return trainingDAO.getAllTrainings();
    }
}
