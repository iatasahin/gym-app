package dev.ilkersahin.java.spring.gym.service.impl;

import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;
import dev.ilkersahin.java.spring.gym.exception.TraineeDoesNotExistException;
import dev.ilkersahin.java.spring.gym.exception.TrainerDoesNotExistException;
import dev.ilkersahin.java.spring.gym.metrics.TrainingMetrics;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.repository.TraineeRepository;
import dev.ilkersahin.java.spring.gym.repository.TrainerRepository;
import dev.ilkersahin.java.spring.gym.repository.TrainingRepository;
import dev.ilkersahin.java.spring.gym.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TrainingServiceImpl implements TrainingService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingMetrics  trainingMetrics;

    @Override
    public void createTraining(@Valid TrainingCreateRequest request) {

        Trainee trainee = findTraineeOrThrow(request.traineeUsername());

        Trainer trainer = trainerRepository.findByUserUsername(request.trainerUsername())
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

        trainingRepository.save(training);

        trainingMetrics.incrementTrainings();
        trainingMetrics.addTrainingDuration(request.durationMinutes());
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private Trainee findTraineeOrThrow(String username) {
        return traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new TraineeDoesNotExistException("Trainee not found"));
    }
}
