package dev.ilkersahin.java.spring.gym.xmlfileIO.dto;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "gym-data")
@XmlAccessorType(XmlAccessType.FIELD)
public class FileContentXml {
    @XmlElementWrapper(name = "trainers")
    @XmlElement(name = "trainer")
    List<TrainerXml> trainers;

    @XmlElementWrapper(name = "trainees")
    @XmlElement(name = "trainee")
    List<TraineeXml> trainees;

    @XmlElementWrapper(name = "trainings")
    @XmlElement(name = "training")
    List<TrainingXml> trainings;

    public List<TrainerXml> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<TrainerXml> trainers) {
        this.trainers = trainers;
    }

    public List<TraineeXml> getTrainees() {
        return trainees;
    }

    public void setTrainees(List<TraineeXml> trainees) {
        this.trainees = trainees;
    }

    public List<TrainingXml> getTrainings() {
        return trainings;
    }

    public void setTrainings(List<TrainingXml> trainings) {
        this.trainings = trainings;
    }
}
