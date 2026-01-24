package dev.ilkersahin.java.spring.gym.service.impl;

import dev.ilkersahin.java.spring.gym.dao.TraineeDao;
import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.dao.TrainingDao;
import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.service.TrainingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;

    @Override
    public void createTraining(@Valid TrainingCreateRequest request) {

        Trainee trainee = authenticateTrainee(request.credentials());

        Trainer trainer = trainerDao.getTrainer(request.trainerUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found"));

        log.info(
                "Creating training '{}' for trainee '{}' with trainer '{}' on date '{}' for '{}' minutes",
                request.trainingName(),
                trainee.getUser().getUsername(),
                trainer.getUser().getUsername(),
                request.trainingDate(),
                request.durationMinutes()
        );

        Training training = new Training(
                trainee,
                trainer,
                request.trainingName(),
                TrainingType.fromEnum(request.trainingType()),
                request.trainingDate(),
                Duration.ofMinutes(request.durationMinutes())
        );

        trainingDao.createTraining(training);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private Trainee authenticateTrainee(Credentials credentials) {
        Trainee trainee = traineeDao.getTrainee(credentials.username())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found"));

        if (!trainee.getUser().getPassword().equals(credentials.password())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (!trainee.getUser().isActive()) {
            throw new IllegalStateException("Trainee is inactive");
        }
        return trainee;
    }
}
