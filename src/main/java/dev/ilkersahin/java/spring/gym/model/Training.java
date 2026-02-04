package dev.ilkersahin.java.spring.gym.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "trainings",
        indexes = {
                @Index(name = "idx_training_date", columnList = "training_date"),
                @Index(name = "idx_training_trainer", columnList = "trainer_id"),
                @Index(name = "idx_training_trainee", columnList = "trainee_id"),
                @Index(name = "idx_training_trainer_trainee", columnList = "trainer_id, trainee_id"),
                @Index(name = "idx_training_trainee_trainer", columnList = "trainee_id, trainer_id"),
                @Index(name = "idx_training_training_type", columnList = "training_type_id")
        }
)
@Setter
@Getter
@ToString
@NoArgsConstructor
public class Training {

    public static record TrainingKey(
            UUID trainerId,
            UUID traineeId,
            LocalDate trainingDate
    ) {
    }

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "training_id", updatable = false, nullable = false)
    private UUID trainingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "trainee_id",
            referencedColumnName = "trainee_id",
            nullable = false
    )
    private Trainee trainee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "trainer_id",
            referencedColumnName = "trainer_id",
            nullable = false
    )
    private Trainer trainer;

    @Column(name = "training_name", nullable = false, length = 100)
    private String trainingName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "training_type_id",
            referencedColumnName = "training_type_id",
            nullable = false
    )
    private TrainingType trainingType;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "training_duration_minutes", nullable = false)
    private Integer trainingDuration;

    public Training(Trainee trainee, Trainer trainer, String trainingName,
                    TrainingType trainingType, LocalDate trainingDate, int trainingDuration
    ) {
        this.trainee = trainee;
        this.trainer = trainer;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Training training = (Training) o;
        return Objects.equals(trainee, training.trainee) &&
                Objects.equals(trainer, training.trainer) &&
                Objects.equals(trainingName, training.trainingName) &&
                Objects.equals(trainingType, training.trainingType) &&
                Objects.equals(trainingDate, training.trainingDate) &&
                Objects.equals(trainingDuration, training.trainingDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainee, trainer, trainingName, trainingType, trainingDate, trainingDuration);
    }
}
