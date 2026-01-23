package dev.ilkersahin.java.spring.gym.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "trainees")
@PrimaryKeyJoinColumn(name = "trainee_id", referencedColumnName = "user_id")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class Trainee extends User {

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
        super(firstName, lastName, username, password, isActive);

        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public UUID getTraineeId() {
        return getUserId();
    }

    public void setTraineeId(UUID id){
        setUserId(id);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Trainee trainee = (Trainee) o;
        return Objects.equals(getUserId(), trainee.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId());
    }

}
