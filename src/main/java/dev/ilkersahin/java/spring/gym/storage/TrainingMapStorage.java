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
        Training.TrainingKey key = new Training.TrainingKey(
                training.getTrainerId(),
                training.getTraineeId(),
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
        Training.TrainingKey key = new Training.TrainingKey(trainerId, traineeId, trainingDate);

        List<Training> result = trainingsByTrainingKey.getOrDefault(key, List.of());

        log.debug("Lookup trainings for key {} → {} found", key, result.size());

        return result;
    }
}
