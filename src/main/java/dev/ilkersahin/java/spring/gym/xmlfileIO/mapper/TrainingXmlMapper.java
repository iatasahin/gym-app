package dev.ilkersahin.java.spring.gym.xmlfileIO.mapper;

import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TrainingXml;

import java.time.Duration;

public class TrainingXmlMapper {
    public static TrainingXml toXml(TrainingCreateRequest t) {
        TrainingXml x = new TrainingXml();
        x.setTraineeUsername(t.credentials().username());
        x.setTraineePassword(t.credentials().password());
        x.setTrainerUsername(t.trainerUsername());
        x.setTrainingName(t.trainingName());
        x.setTrainingType(t.trainingType());
        x.setTrainingDate(t.trainingDate());
        x.setTrainingDuration(Duration.ofMinutes(t.durationMinutes()));
        return x;
    }

    public static TrainingCreateRequest toDomain(TrainingXml x) {
        return new TrainingCreateRequest(
                new Credentials(
                        x.getTraineeUsername(),
                        x.getTraineePassword()
                ),
                x.getTrainerUsername(),
                x.getTrainingName(),
                x.getTrainingType(),
                x.getTrainingDate(),
                (int) x.getTrainingDuration().toMinutes()
        );
    }
}
