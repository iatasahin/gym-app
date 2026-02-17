package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TraineeRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private UserRepository userRepository;

    private User traineeUser;
    private Trainee trainee;

    @BeforeEach
    void setUp() {
        traineeUser = new User("John", "Doe", "John.Doe." + System.nanoTime(), "pass", true);
        userRepository.save(traineeUser);

        trainee = new Trainee();
        trainee.setUser(traineeUser);
        trainee.setDateOfBirth(LocalDate.of(1990, 5, 15));
        trainee.setAddress("123 Main St");
    }

    // =========================================================================
    // SAVE TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void save_withValidTrainee_shouldPersist() {
        Trainee saved = traineeRepository.save(trainee);

        assertThat(saved.getTraineeId()).isNotNull();
        assertThat(saved.getUser().getUsername()).isEqualTo(traineeUser.getUsername());
    }

    // =========================================================================
    // FIND BY USERNAME TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void findByUserUsername_withExistingTrainee_shouldReturnTrainee() {
        traineeRepository.save(trainee);

        Optional<Trainee> found = traineeRepository.findByUserUsername(traineeUser.getUsername());

        assertThat(found).isPresent();
        assertThat(found.get().getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
    }

    @Test
    @Order(202)
    void findByUserUsername_withNonExistingTrainee_shouldReturnEmpty() {
        Optional<Trainee> found = traineeRepository.findByUserUsername("NonExistent.Trainee");

        assertThat(found).isEmpty();
    }

    // =========================================================================
    // FIND ASSIGNED TRAINERS TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void findAssignedTrainers_withNoTrainers_shouldReturnEmptyList() {
        traineeRepository.save(trainee);

        List<Trainer> trainers = traineeRepository.findAssignedTrainers(traineeUser.getUsername());

        assertThat(trainers).isEmpty();
    }

    @Test
    @Order(302)
    void findAssignedTrainers_withAssignedTrainers_shouldReturnTrainerList() {
        // Create trainer
        User trainerUser = new User("Tom", "Trainer", "Tom.Trainer." + System.nanoTime(), "pass", true);
        userRepository.save(trainerUser);

        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(TrainingType.fromEnum(TrainingType.Type.FITNESS));
        trainerRepository.save(trainer);

        // Assign trainer to trainee
        trainee.getTrainers().add(trainer);
        traineeRepository.save(trainee);

        List<Trainer> trainers = traineeRepository.findAssignedTrainers(traineeUser.getUsername());

        assertThat(trainers).hasSize(1);
        assertThat(trainers.getFirst().getUser().getUsername()).isEqualTo(trainerUser.getUsername());
    }

    // =========================================================================
    // DELETE TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void delete_withExistingTrainee_shouldRemove() {
        Trainee saved = traineeRepository.save(trainee);

        traineeRepository.delete(saved);
        traineeRepository.flush();

        Optional<Trainee> found = traineeRepository.findByUserUsername(traineeUser.getUsername());
        assertThat(found).isEmpty();
    }
}
