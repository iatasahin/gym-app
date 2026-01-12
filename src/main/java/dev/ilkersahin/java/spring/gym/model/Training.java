package dev.ilkersahin.java.spring.gym.model;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Training {

    public static record TrainingKey(
            UUID trainerId,
            UUID traineeId,
            LocalDate trainingDate
    ) {
    }

    private UUID traineeId;
    private UUID trainerId;
    private String trainingName;
    private TrainingType trainingType;
    private LocalDate trainingDate;
    private Duration trainingDuration;

    public Training(UUID traineeId, UUID trainerId, String trainingName, TrainingType trainingType, LocalDate trainingDate, Duration trainingDuration) {
        this.traineeId = traineeId;
        this.trainerId = trainerId;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    public Training() {
    }

    public UUID getTraineeId() {
        return traineeId;
    }

    public void setTraineeId(UUID traineeId) {
        this.traineeId = traineeId;
    }

    public UUID getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(UUID trainerId) {
        this.trainerId = trainerId;
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
                "traineeId=" + traineeId +
                ", trainerId=" + trainerId +
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
        return Objects.equals(traineeId, training.traineeId) &&
                Objects.equals(trainerId, training.trainerId) &&
                Objects.equals(trainingName, training.trainingName) &&
                Objects.equals(trainingType, training.trainingType) &&
                Objects.equals(trainingDate, training.trainingDate) &&
                Objects.equals(trainingDuration, training.trainingDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traineeId, trainerId, trainingName, trainingType, trainingDate, trainingDuration);
    }
}
