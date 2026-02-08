package dev.ilkersahin.java.spring.gym.service.impl;

import dev.ilkersahin.java.spring.gym.dao.TraineeDao;
import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.dao.TrainingDao;
import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;
import dev.ilkersahin.java.spring.gym.exception.TraineeDoesNotExistException;
import dev.ilkersahin.java.spring.gym.exception.TrainerDoesNotExistException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Trainee trainee = findTraineeOrThrow(request.traineeUsername());

        Trainer trainer = trainerDao.getTrainer(request.trainerUsername())
                .orElseThrow(() -> new TrainerDoesNotExistException("Trainer not found"));

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
                TrainingType.fromEnum(TrainingType.Type.fromName(request.trainingType())),
                request.trainingDate(),
                request.durationMinutes()
        );

        trainingDao.createTraining(training);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private Trainee findTraineeOrThrow(String username) {
        Trainee trainee = traineeDao.getTrainee(username)
                .orElseThrow(() -> new TraineeDoesNotExistException("Trainee not found"));
        return trainee;
    }
}
