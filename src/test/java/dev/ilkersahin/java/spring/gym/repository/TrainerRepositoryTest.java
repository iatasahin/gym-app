package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TrainerRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private UserRepository userRepository;

    private User trainerUser;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        trainerUser = new User("Jane", "Trainer", "Jane.Trainer." + System.nanoTime(), "pass", true);
        userRepository.save(trainerUser);

        trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(TrainingType.fromEnum(TrainingType.Type.YOGA));
    }

    // =========================================================================
    // SAVE TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void save_withValidTrainer_shouldPersist() {
        Trainer saved = trainerRepository.save(trainer);

        assertThat(saved.getTrainerId()).isNotNull();
        assertThat(saved.getSpecialization().getType()).isEqualTo(TrainingType.Type.YOGA);
    }

    // =========================================================================
    // FIND BY USERNAME TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void findByUserUsername_withExistingTrainer_shouldReturnTrainer() {
        trainerRepository.save(trainer);

        Optional<Trainer> found = trainerRepository.findByUserUsername(trainerUser.getUsername());

        assertThat(found).isPresent();
        assertThat(found.get().getSpecialization().getType()).isEqualTo(TrainingType.Type.YOGA);
    }

    // =========================================================================
    // FIND BY USERNAMES TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void findByUserUsernames_withMultipleUsernames_shouldReturnMatchingTrainers() {
        trainerRepository.save(trainer);

        User trainerUser2 = new User("Bob", "Coach", "Bob.Coach." + System.nanoTime(), "pass", true);
        userRepository.save(trainerUser2);

        Trainer trainer2 = new Trainer();
        trainer2.setUser(trainerUser2);
        trainer2.setSpecialization(TrainingType.fromEnum(TrainingType.Type.FITNESS));
        trainerRepository.save(trainer2);

        List<Trainer> found = trainerRepository.findByUserUsernames(
                List.of(trainerUser.getUsername(), trainerUser2.getUsername())
        );

        assertThat(found).hasSize(2);
    }

    @Test
    @Order(302)
    void findByUserUsernames_withEmptyList_shouldReturnEmptyList() {
        List<Trainer> found = trainerRepository.findByUserUsernames(List.of());

        assertThat(found).isEmpty();
    }

    // =========================================================================
    // FIND TRAINERS NOT ASSIGNED TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void findTrainersNotAssignedToTrainee_shouldExcludeAssignedTrainers() {
        // Create trainee
        User traineeUser = new User("Trainee", "One", "Trainee.One." + System.nanoTime(), "pass", true);
        userRepository.save(traineeUser);
        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);

        // Save trainer and assign to trainee
        Trainer savedTrainer = trainerRepository.save(trainer);
        trainee.getTrainers().add(savedTrainer);
        traineeRepository.save(trainee);

        // Create unassigned trainer
        User unassignedUser = new User("Free", "Trainer", "Free.Trainer." + System.nanoTime(), "pass", true);
        userRepository.save(unassignedUser);
        Trainer unassignedTrainer = new Trainer();
        unassignedTrainer.setUser(unassignedUser);
        unassignedTrainer.setSpecialization(TrainingType.fromEnum(TrainingType.Type.STRETCHING));
        trainerRepository.save(unassignedTrainer);

        List<Trainer> notAssigned = trainerRepository.findTrainersNotAssignedToTrainee(traineeUser.getUsername());

        assertThat(notAssigned).hasSize(1);
        assertThat(notAssigned.getFirst().getUser().getUsername()).isEqualTo(unassignedUser.getUsername());
    }
}
