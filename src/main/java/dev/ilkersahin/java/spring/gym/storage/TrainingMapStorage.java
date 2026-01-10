package dev.ilkersahin.java.spring.gym.storage;

import dev.ilkersahin.java.spring.gym.dao.TrainingDAO;
import dev.ilkersahin.java.spring.gym.model.Training;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class TrainingMapStorage implements TrainingDAO {
    private final ConcurrentMap<Training.TrainingKey, List<Training>> trainingsByTrainingKey = new ConcurrentHashMap<>();

    @Override
    public Training createTraining(Training training) {
        List<Training> trainingsOnDay = trainingsByTrainingKey.putIfAbsent(
                new Training.TrainingKey(
                        training.getTrainerId(),
                        training.getTraineeId(),
                        training.getTrainingDate()
                ),
                List.of(training)
        );
        if(trainingsOnDay !=  null){
            trainingsOnDay.add(training);
        }
        return training;
    }

    @Override
    public List<Training> getAllTrainings() {
        return trainingsByTrainingKey.values().stream().flatMap(List::stream).toList();
    }

    @Override
    public List<Training> getTraining(UUID traineeId, UUID trainerId, LocalDate trainingDate) {
        return trainingsByTrainingKey.getOrDefault(
                new Training.TrainingKey(trainerId, traineeId, trainingDate),
                List.of()
        );
    }
}
