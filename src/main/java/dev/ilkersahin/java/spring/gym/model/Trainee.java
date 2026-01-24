package dev.ilkersahin.java.spring.gym.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "trainees")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class Trainee {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "trainee_id", updatable = false, nullable = false)
    private UUID traineeId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address", length = 255)
    private String address;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "trainees_trainers",
            joinColumns = @JoinColumn(name = "trainee_id", referencedColumnName = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id", referencedColumnName = "trainer_id")
    )
    @ToString.Exclude
    private Set<Trainer> trainers;

    @OneToMany(
            mappedBy = "trainee",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @ToString.Exclude
    private Set<Training> trainings;

    public Trainee(String firstName, String lastName, String username, String password, boolean isActive, LocalDate dateOfBirth, String address, UUID traineeId) {
        user = new User(firstName, lastName, username, password, isActive);

        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

//    --------------------- Getters/Setters delegating to user --------------------- //

    public UUID getUserId() {
        if (user == null) return null;
        return user.getUserId();
    }

    public void setUserId(UUID userId) {
        if (user == null) return;
        user.setUserId(userId);
    }

    public boolean isActive() {
        if (user == null) return false;
        return user.isActive();
    }

    public void setActive(boolean active) {
        if (user == null) return;
        user.setActive(active);
    }

    public String getPassword() {
        if (user == null) return null;
        return user.getPassword();
    }

    public void setPassword(String password) {
        if (user == null) return;
        user.setPassword(password);
    }

    public String getUsername() {
        if (user == null) return null;
        return user.getUsername();
    }

    public void setUsername(String username) {
        if (user == null) return;
        user.setUsername(username);
    }

    public String getLastName() {
        if (user == null) return null;
        return user.getLastName();
    }

    public void setLastName(String lastName) {
        if (user == null) return;
        user.setLastName(lastName);
    }

    public String getFirstName() {
        if (user == null) return null;
        return user.getFirstName();
    }

    public void setFirstName(String firstName) {
        if (user == null) return;
        user.setFirstName(firstName);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Trainee trainee = (Trainee) o;
        return Objects.equals(getTraineeId(), trainee.traineeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTraineeId());
    }

}
