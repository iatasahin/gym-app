package dev.ilkersahin.java.spring.gym.model;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Training {

    public static record TrainingKey(
            UUID trainerID,
            UUID traineeID,
            LocalDate trainingDate
    ) {
    }

    private UUID traineeID;
    private UUID trainerID;
    private String trainingName;
    private TrainingType trainingType;
    private LocalDate trainingDate;
    private Duration trainingDuration;

    public Training(UUID traineeID, UUID trainerID, String trainingName, TrainingType trainingType, LocalDate trainingDate, Duration trainingDuration) {
        this.traineeID = traineeID;
        this.trainerID = trainerID;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    public Training() {
    }

    public UUID getTraineeID() {
        return traineeID;
    }

    public void setTraineeID(UUID traineeID) {
        this.traineeID = traineeID;
    }

    public UUID getTrainerID() {
        return trainerID;
    }

    public void setTrainerID(UUID trainerID) {
        this.trainerID = trainerID;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public Duration getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Duration trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    @Override
    public String toString() {
        return "Training{" +
                "traineeID=" + traineeID +
                ", trainerID=" + trainerID +
                ", trainingName='" + trainingName + '\'' +
                ", trainingType=" + trainingType +
                ", trainingDate=" + trainingDate +
                ", trainingDuration=" + trainingDuration +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Training training = (Training) o;
        return Objects.equals(traineeID, training.traineeID) &&
                Objects.equals(trainerID, training.trainerID) &&
                Objects.equals(trainingName, training.trainingName) &&
                Objects.equals(trainingType, training.trainingType) &&
                Objects.equals(trainingDate, training.trainingDate) &&
                Objects.equals(trainingDuration, training.trainingDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traineeID, trainerID, trainingName, trainingType, trainingDate, trainingDuration);
    }
}
