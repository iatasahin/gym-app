package dev.ilkersahin.java.spring.gym.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "trainers",
        indexes = {
                @Index(name = "idx_trainers_specialization", columnList = "specialization_id"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Trainer {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "trainer_id", updatable = false, nullable = false)
    private UUID trainerId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "specialization_id",
            referencedColumnName = "training_type_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_trainers_training_types")
    )
    private TrainingType specialization;

    @ManyToMany(mappedBy = "trainers", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Trainee> trainees = new HashSet<>();

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Training> trainings = new HashSet<>();


    public TrainingType.Type getSpecializationType() {
        return specialization != null ? specialization.toEnum() : null;
    }

    public void setSpecializationType(TrainingType.Type type) {
        this.specialization = TrainingType.fromEnum(type);
    }

    public Trainer(User user, TrainingType specialization) {
        this.user = user;
        this.specialization = specialization;
    }

    public Trainer(
            String firstName, String lastName, String username, String password, boolean isActive,
            TrainingType specialization
    ) {
        user = new User(firstName, lastName, username, password, isActive);

        this.specialization = specialization;
    }

    // Legacy Constructor
    public Trainer(
            String firstName, String lastName, String username, String password, boolean isActive,
            TrainingType.Type specialization, UUID trainerId
    ) {
        this(firstName, lastName, username, password, isActive, TrainingType.fromEnum(specialization));
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
        Trainer trainer = (Trainer) o;
        return Objects.equals(getTrainerId(), trainer.getTrainerId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTrainerId());
    }

}
