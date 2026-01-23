package dev.ilkersahin.java.spring.gym.xmlfileIO.mapper;

import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.xmlfileIO.dto.TrainingXml;

public class TrainingXmlMapper {

    public static TrainingXml toXml(Training t) {
        TrainingXml x = new TrainingXml();

        x.setTraineeId(t.getTrainee().getTraineeId());
        x.setTrainerId(t.getTrainer().getTrainerId());
        x.setTrainingName(t.getTrainingName());
        x.setTrainingType(t.getTrainingType());
        x.setTrainingDate(t.getTrainingDate());
        x.setTrainingDuration(t.getTrainingDuration());

        return x;
    }

    public static Training toDomain(TrainingXml x) {
        Training t = new Training();

        t.getTrainee().setTraineeId(x.getTraineeId());
        t.getTrainer().setTrainerId(x.getTrainerId());
        t.setTrainingName(x.getTrainingName());
        t.setTrainingType(x.getTrainingType());
        t.setTrainingDate(x.getTrainingDate());
        t.setTrainingDuration(x.getTrainingDuration());

        return t;
    }

}
