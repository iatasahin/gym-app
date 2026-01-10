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
                        training.getTrainerID(),
                        training.getTraineeID(),
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
    public List<Training> getTraining(UUID traineeID, UUID trainerID, LocalDate trainingDate) {
        return trainingsByTrainingKey.getOrDefault(
                new Training.TrainingKey(trainerID, traineeID, trainingDate),
                List.of()
        );
    }
}
