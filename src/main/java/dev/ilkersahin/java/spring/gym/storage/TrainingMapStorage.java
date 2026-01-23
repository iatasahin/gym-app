package dev.ilkersahin.java.spring.gym.storage;

import dev.ilkersahin.java.spring.gym.dao.TrainingDAO;
import dev.ilkersahin.java.spring.gym.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class TrainingMapStorage implements TrainingDAO {
    private static final Logger log = LoggerFactory.getLogger(TrainingMapStorage.class);

    private final ConcurrentMap<Training.TrainingKey, List<Training>> trainingsByTrainingKey = new ConcurrentHashMap<>();

    @Override
    public Training createTraining(Training training) {
        if (training.getTrainer() == null || training.getTrainer().getTrainerId() == null ||
                training.getTrainee() == null || training.getTrainee().getTraineeId() == null ||
                training.getTrainingDate() == null
        ) {
            log.warn("Attempt to Create Training with null parameters: trainee={}, trainer={}, trainingDate={}",
                    training.getTrainer(), training.getTrainee(), training.getTrainingDate());
            throw new NullPointerException(
                    "Attempt to Create Training with null parameters: trainee=%s, trainer=%s, trainingDate=%s"
                            .formatted(training.getTrainer(), training.getTrainee(), training.getTrainingDate())
            );
        }

        Training.TrainingKey key = new Training.TrainingKey(
                training.getTrainer().getTrainerId(),
                training.getTrainee().getTraineeId(),
                training.getTrainingDate()
        );

        log.debug("Storing training under key {}", key);

        trainingsByTrainingKey
                .computeIfAbsent(key, k -> new ArrayList<>())
                .add(training);

        log.info("Training stored for key {} ({} total for this key)",
                key, trainingsByTrainingKey.get(key).size()
        );

        return training;
    }

    @Override
    public List<Training> getAllTrainings() {
        log.debug("Retrieving all trainings ({} total)",
                trainingsByTrainingKey.values().stream().mapToInt(List::size).sum()
        );

        return trainingsByTrainingKey.values().stream().flatMap(List::stream).toList();
    }

    @Override
    public List<Training> getTraining(UUID traineeId, UUID trainerId, LocalDate trainingDate) {
        if (traineeId == null || trainerId == null || trainingDate == null) {
            log.debug("Lookup trainings with null parameters: traineeId={}, trainerId={}, trainingDate={}",
                    traineeId, trainerId, trainingDate);
            return List.of();
        }

        Training.TrainingKey key = new Training.TrainingKey(trainerId, traineeId, trainingDate);
        List<Training> result = trainingsByTrainingKey.getOrDefault(key, List.of());
        log.debug("Lookup trainings for key {} → {} found", key, result.size());
        return result;
    }
}
