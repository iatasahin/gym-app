package dev.ilkersahin.java.spring.gym.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "trainers")
@PrimaryKeyJoinColumn(name = "trainer_id", referencedColumnName = "user_id")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class Trainer extends User {

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
    private Set<Trainee> trainees;

    @OneToMany(
            mappedBy = "trainer",
            fetch = FetchType.LAZY
    )
    @ToString.Exclude
    private Set<Training> trainings;


    public TrainingType.Type getSpecializationType(){
        return specialization != null ? specialization.toEnum() : null;
    }

    public void setSpecializationType(TrainingType.Type type) {
        this.specialization = TrainingType.fromEnum(type);
    }

    public Trainer(
            String firstName, String lastName, String username, String password, boolean isActive,
            TrainingType specialization
    ) {
        super(firstName, lastName, username, password, isActive);

        this.specialization = specialization;
    }

    // Legacy Constructor
    public Trainer(
            String firstName, String lastName, String username, String password, boolean isActive,
            TrainingType.Type specialization, UUID trainerId
    ) {
        this(firstName, lastName, username, password, isActive, TrainingType.fromEnum(specialization));
    }

    public UUID getTrainerId() {
        return getUserId();
    }

    public void setTrainerId(UUID id){
        setUserId(id);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Trainer trainer = (Trainer) o;
        return Objects.equals(getUserId(), trainer.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId());
    }

}
