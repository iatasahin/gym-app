package dev.ilkersahin.java.spring.gym.model;

import java.util.Objects;
import java.util.UUID;

public class Trainer extends User {
    private TrainingType specialization;
    private UUID userID;

    public Trainer(String firstName, String lastname, String userName, String password, boolean isActive, TrainingType specialization, UUID userID) {
        super(firstName, lastname, userName, password, isActive);
        this.specialization = specialization;
        this.userID = userID;
    }

    public Trainer() {
        super();
    }

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }

    public UUID getUserID() {
        return userID;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
    }

    @Override
    public String toString() {
        return "Trainer{" +
                "specialization=" + specialization +
                ", userID=" + userID +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Trainer trainer = (Trainer) o;
        return Objects.equals(userID, trainer.userID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userID);
    }

}
