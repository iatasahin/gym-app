package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.config.TestPersistenceConfig;
import dev.ilkersahin.java.spring.gym.dao.TraineeDao;
import dev.ilkersahin.java.spring.gym.dao.UserDao;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPersistenceConfig.class)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TraineeRepositoryImplTest {
    @Autowired
    private TraineeDao traineeRepository;

    @Autowired
    private UserDao userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User user;
    private Trainee trainee;

    @BeforeEach
    void setUp() {
        user = new User("Jack", "Black", "Jack.Black", "password123", true);
        userRepository.persist(user);

        trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 15));
        trainee.setAddress("123 Main St");
    }

    // =========================================================================
    // CREATE TRAINEE TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void createTrainee_withValidTrainee_shouldPersistAndReturn() {
        Trainee created = traineeRepository.createTrainee(trainee);

        assertThat(created).isNotNull();
        assertThat(created.getTraineeId()).isNotNull();
        assertThat(created.getUser().getUsername()).isEqualTo("Jack.Black");
    }

    @Test
    @Order(102)
    void createTrainee_withValidTrainee_shouldBeRetrievable() {
        traineeRepository.createTrainee(trainee);

        Optional<Trainee> found = traineeRepository.getTrainee("Jack.Black");

        assertThat(found).isPresent();
        assertThat(found.get().getAddress()).isEqualTo("123 Main St");
    }

    // =========================================================================
    // UPDATE TRAINEE TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void updateTrainee_withExistingTrainee_shouldUpdateFields() {
        traineeRepository.createTrainee(trainee);

        trainee.setAddress("456 Oak Ave");
        trainee.setDateOfBirth(LocalDate.of(1985, 6, 20));

        Trainee updated = traineeRepository.updateTrainee(trainee);

        assertThat(updated.getAddress()).isEqualTo("456 Oak Ave");
        assertThat(updated.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 6, 20));
    }

    // =========================================================================
    // DELETE TRAINEE TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void deleteTrainee_withExistingUsername_shouldDeleteAndReturn() {
        traineeRepository.createTrainee(trainee);

        Optional<Trainee> deleted = traineeRepository.deleteTrainee("Jack.Black");

        assertThat(deleted).isPresent();
        assertThat(deleted.get().getUser().getUsername()).isEqualTo("Jack.Black");
    }

    @Test
    @Order(302)
    void deleteTrainee_withExistingUsername_shouldRemoveFromDatabase() {
        traineeRepository.createTrainee(trainee);

        traineeRepository.deleteTrainee("Jack.Black");
        entityManager.flush();
        entityManager.clear();

        Optional<Trainee> found = traineeRepository.getTrainee("Jack.Black");

        assertThat(found).isEmpty();
    }

    @Test
    @Order(303)
    void deleteTrainee_withNonExistingUsername_shouldReturnEmpty() {
        Optional<Trainee> deleted = traineeRepository.deleteTrainee("Non.Existent");

        assertThat(deleted).isEmpty();
    }

    // =========================================================================
    // GET TRAINEE TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void getTrainee_withExistingUsername_shouldReturnTrainee() {
        traineeRepository.createTrainee(trainee);

        Optional<Trainee> found = traineeRepository.getTrainee("Jack.Black");

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getFirstName()).isEqualTo("Jack");
    }

    @Test
    @Order(402)
    void getTrainee_withNonExistingUsername_shouldReturnEmpty() {
        Optional<Trainee> found = traineeRepository.getTrainee("Non.Existent");

        assertThat(found).isEmpty();
    }

    // =========================================================================
    // GET ALL TRAINEES TESTS (500s)
    // =========================================================================

    @Test
    @Order(501)
    void getAllTrainees_withNoTrainees_shouldReturnEmptyList() {
        List<Trainee> trainees = traineeRepository.getAllTrainees();

        assertThat(trainees).isEmpty();
    }

    @Test
    @Order(502)
    void getAllTrainees_withMultipleTrainees_shouldReturnAllOrdered() {
        // Create first trainee
        traineeRepository.createTrainee(trainee);

        // Create second trainee
        User user2 = new User("Alice", "Smith", "Alice.Smith", "pass", true);
        userRepository.persist(user2);
        Trainee trainee2 = new Trainee();
        trainee2.setUser(user2);
        traineeRepository.createTrainee(trainee2);

        List<Trainee> trainees = traineeRepository.getAllTrainees();

        assertThat(trainees).hasSize(2);
        assertThat(trainees.get(0).getUser().getUsername()).isEqualTo("Alice.Smith");
        assertThat(trainees.get(1).getUser().getUsername()).isEqualTo("Jack.Black");
    }

    // =========================================================================
    // FIND ASSIGNED TRAINERS TESTS (600s)
    // =========================================================================

    @Test
    @Order(601)
    void findAssignedTrainers_withNoTrainers_shouldReturnEmptyList() {
        traineeRepository.createTrainee(trainee);

        List<Trainer> trainers = traineeRepository.findAssignedTrainers("Jack.Black");

        assertThat(trainers).isEmpty();
    }

    @Test
    @Order(602)
    void findAssignedTrainers_withAssignedTrainers_shouldReturnTrainers() {
        traineeRepository.createTrainee(trainee);

        // Create training type
        TrainingType trainingType = TrainingType.fromEnum(TrainingType.Type.FITNESS);
        entityManager.persist(trainingType);

        // Create trainer
        User trainerUser = new User("Tom", "Trainer", "Tom.Trainer", "pass", true);
        userRepository.persist(trainerUser);
        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(trainingType);
        entityManager.persist(trainer);

        // Assign trainer to trainee
        trainee.getTrainers().add(trainer);
        traineeRepository.updateTrainee(trainee);

        List<Trainer> trainers = traineeRepository.findAssignedTrainers("Jack.Black");

        assertThat(trainers).hasSize(1);
        assertThat(trainers.get(0).getUser().getUsername()).isEqualTo("Tom.Trainer");
    }

    // =========================================================================
    // UPDATE TRAINERS TESTS (700s)
    // =========================================================================

    @Test
    @Order(701)
    void updateTrainers_withNewTrainers_shouldReplaceTrainerList() {
        traineeRepository.createTrainee(trainee);

        // Create training type
        TrainingType trainingType = TrainingType.fromEnum(TrainingType.Type.FITNESS);
        entityManager.persist(trainingType);

        // Create trainers
        User trainerUser1 = new User("Tom", "One", "Tom.One", "pass", true);
        User trainerUser2 = new User("Jane", "Two", "Jane.Two", "pass", true);
        userRepository.persist(trainerUser1);
        userRepository.persist(trainerUser2);

        Trainer trainer1 = new Trainer();
        trainer1.setUser(trainerUser1);
        trainer1.setSpecialization(trainingType);
        entityManager.persist(trainer1);

        Trainer trainer2 = new Trainer();
        trainer2.setUser(trainerUser2);
        trainer2.setSpecialization(trainingType);
        entityManager.persist(trainer2);

        traineeRepository.updateTrainers("Jack.Black", List.of(trainer1, trainer2));

        List<Trainer> trainers = traineeRepository.findAssignedTrainers("Jack.Black");

        assertThat(trainers).hasSize(2);
    }

    @Test
    @Order(702)
    void updateTrainers_withEmptyList_shouldClearTrainers() {
        traineeRepository.createTrainee(trainee);

        // Create and assign a trainer first
        TrainingType trainingType = TrainingType.fromEnum(TrainingType.Type.FITNESS);
        entityManager.persist(trainingType);

        User trainerUser = new User("Tom", "Trainer", "Tom.Trainer", "pass", true);
        userRepository.persist(trainerUser);
        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(trainingType);
        entityManager.persist(trainer);

        traineeRepository.updateTrainers("Jack.Black", List.of(trainer));

        // Now clear
        traineeRepository.updateTrainers("Jack.Black", List.of());

        List<Trainer> trainers = traineeRepository.findAssignedTrainers("Jack.Black");

        assertThat(trainers).isEmpty();
    }

    @Test
    @Order(703)
    void updateTrainers_withNonExistingTrainee_shouldThrowException() {
        assertThatThrownBy(() -> traineeRepository.updateTrainers("Non.Existent", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trainee not found");
    }
}
