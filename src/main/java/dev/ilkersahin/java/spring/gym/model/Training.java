package dev.ilkersahin.java.spring.gym.model;

import dev.ilkersahin.java.spring.gym.model.converter.DurationConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "trainings")
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

    @Convert(converter = DurationConverter.class)
    @Column(name = "training_duration_minutes", nullable = false)
    private Duration trainingDuration;

    public Training(Trainee trainee, Trainer trainer, String trainingName,
                    TrainingType trainingType, LocalDate trainingDate, Duration trainingDuration
    ) {
        this.trainee = trainee;
        this.trainer = trainer;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    // Legacy Constructor
    public Training(UUID traineeId, UUID trainerId, String trainingName,
                    TrainingType.Type trainingType, LocalDate trainingDate, Duration trainingDuration
    ) {
        this(new Trainee(), new Trainer(), trainingName, TrainingType.fromEnum(trainingType), trainingDate, trainingDuration);
        trainee.setTraineeId(traineeId);
        trainer.setTrainerId(trainerId);
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
