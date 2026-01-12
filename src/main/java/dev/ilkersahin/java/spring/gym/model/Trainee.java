package dev.ilkersahin.java.spring.gym.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Trainee extends User {
    private LocalDate dateOfBirth;
    private String address;
    private UUID userId;

    public Trainee(String firstName, String lastName, String username, String password, boolean isActive, LocalDate dateOfBirth, String address, UUID userId) {
        super(firstName, lastName, username, password, isActive);
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.userId = userId;
    }

    public Trainee() {
        super();
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Trainee{" +
                "dateOfBirth=" + dateOfBirth +
                ", address='" + address + '\'' +
                ", userId=" + userId +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Trainee trainee = (Trainee) o;
        return Objects.equals(userId, trainee.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId);
    }

}
