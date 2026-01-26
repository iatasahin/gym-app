package dev.ilkersahin.java.spring.gym.repository;


import dev.ilkersahin.java.spring.gym.config.TestPersistenceConfig;
import dev.ilkersahin.java.spring.gym.dao.TraineeDao;
import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPersistenceConfig.class)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TrainerRepositoryImplTest {
    @Autowired
    private TrainerDao trainerRepository;

    @Autowired
    private TraineeDao traineeRepository;

    @Autowired
    private UserDao userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User user;
    private Trainer trainer;
    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        // Create training type
        trainingType = TrainingType.fromEnum(TrainingType.Type.FITNESS);
        entityManager.persist(trainingType);

        // Create user
        user = new User("Tom", "Trainer", "Tom.Trainer", "password123", true);
        userRepository.persist(user);

        // Create trainer
        trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(trainingType);
    }

    // =========================================================================
    // CREATE TRAINER TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void createTrainer_withValidTrainer_shouldPersistAndReturn() {
        Trainer created = trainerRepository.createTrainer(trainer);

        assertThat(created).isNotNull();
        assertThat(created.getTrainerId()).isNotNull();
        assertThat(created.getUser().getUsername()).isEqualTo("Tom.Trainer");
    }

    @Test
    @Order(102)
    void createTrainer_withValidTrainer_shouldBeRetrievable() {
        trainerRepository.createTrainer(trainer);

        Optional<Trainer> found = trainerRepository.getTrainer("Tom.Trainer");

        assertThat(found).isPresent();
        assertThat(found.get().getSpecialization().getTrainingTypeName())
                .isEqualTo(TrainingType.Type.FITNESS.getName());
    }

    // =========================================================================
    // UPDATE TRAINER TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void updateTrainer_withExistingTrainer_shouldUpdateFields() {
        trainerRepository.createTrainer(trainer);

        TrainingType yoga = TrainingType.fromEnum(TrainingType.Type.YOGA);
        entityManager.persist(yoga);

        trainer.setSpecialization(yoga);
        Trainer updated = trainerRepository.updateTrainer(trainer);

        assertThat(updated.getSpecialization().getTrainingTypeName())
                .isEqualTo(TrainingType.Type.YOGA.getName());
    }

    // =========================================================================
    // GET TRAINER TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void getTrainer_withExistingUsername_shouldReturnTrainer() {
        trainerRepository.createTrainer(trainer);

        Optional<Trainer> found = trainerRepository.getTrainer("Tom.Trainer");

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getFirstName()).isEqualTo("Tom");
    }

    @Test
    @Order(302)
    void getTrainer_withNonExistingUsername_shouldReturnEmpty() {
        Optional<Trainer> found = trainerRepository.getTrainer("Non.Existent");

        assertThat(found).isEmpty();
    }

    // =========================================================================
    // GET ALL TRAINERS TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void getAllTrainers_withNoTrainers_shouldReturnEmptyList() {
        List<Trainer> trainers = trainerRepository.getAllTrainers();

        assertThat(trainers).isEmpty();
    }

    @Test
    @Order(402)
    void getAllTrainers_withMultipleTrainers_shouldReturnAllOrdered() {
        trainerRepository.createTrainer(trainer);

        // Create second trainer
        User user2 = new User("Alice", "Coach", "Alice.Coach", "pass", true);
        userRepository.persist(user2);
        Trainer trainer2 = new Trainer();
        trainer2.setUser(user2);
        trainer2.setSpecialization(trainingType);
        trainerRepository.createTrainer(trainer2);

        List<Trainer> trainers = trainerRepository.getAllTrainers();

        assertThat(trainers).hasSize(2);
        assertThat(trainers.get(0).getUser().getUsername()).isEqualTo("Alice.Coach");
        assertThat(trainers.get(1).getUser().getUsername()).isEqualTo("Tom.Trainer");
    }

    // =========================================================================
    // FIND TRAINERS NOT ASSIGNED TO TRAINEE TESTS (500s)
    // =========================================================================

    @Test
    @Order(501)
    void findTrainersNotAssignedToTrainee_withNoAssignment_shouldReturnAllTrainers() {
        trainerRepository.createTrainer(trainer);

        // Create trainee without any trainers
        User traineeUser = new User("Jack", "Black", "Jack.Black", "pass", true);
        userRepository.persist(traineeUser);
        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);
        traineeRepository.createTrainee(trainee);

        List<Trainer> unassigned = trainerRepository.findTrainersNotAssignedToTrainee("Jack.Black");

        assertThat(unassigned).hasSize(1);
        assertThat(unassigned.get(0).getUser().getUsername()).isEqualTo("Tom.Trainer");
    }

    @Test
    @Order(502)
    void findTrainersNotAssignedToTrainee_withAssignedTrainer_shouldExcludeAssigned() {
        trainerRepository.createTrainer(trainer);

        // Create second trainer
        User user2 = new User("Jane", "Coach", "Jane.Coach", "pass", true);
        userRepository.persist(user2);
        Trainer trainer2 = new Trainer();
        trainer2.setUser(user2);
        trainer2.setSpecialization(trainingType);
        trainerRepository.createTrainer(trainer2);

        // Create trainee and assign first trainer
        User traineeUser = new User("Jack", "Black", "Jack.Black", "pass", true);
        userRepository.persist(traineeUser);
        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);
        trainee.getTrainers().add(trainer);
        traineeRepository.createTrainee(trainee);

        List<Trainer> unassigned = trainerRepository.findTrainersNotAssignedToTrainee("Jack.Black");

        assertThat(unassigned).hasSize(1);
        assertThat(unassigned.get(0).getUser().getUsername()).isEqualTo("Jane.Coach");
    }

    @Test
    @Order(503)
    void findTrainersNotAssignedToTrainee_withAllAssigned_shouldReturnEmptyList() {
        trainerRepository.createTrainer(trainer);

        // Create trainee and assign the trainer
        User traineeUser = new User("Jack", "Black", "Jack.Black", "pass", true);
        userRepository.persist(traineeUser);
        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);
        trainee.getTrainers().add(trainer);
        traineeRepository.createTrainee(trainee);

        List<Trainer> unassigned = trainerRepository.findTrainersNotAssignedToTrainee("Jack.Black");

        assertThat(unassigned).isEmpty();
    }

    // =========================================================================
    // FIND BY USERNAMES TESTS (600s)
    // =========================================================================

    @Test
    @Order(601)
    void findByUsernames_withValidUsernames_shouldReturnMatchingTrainers() {
        trainerRepository.createTrainer(trainer);

        // Create second trainer
        User user2 = new User("Jane", "Coach", "Jane.Coach", "pass", true);
        userRepository.persist(user2);
        Trainer trainer2 = new Trainer();
        trainer2.setUser(user2);
        trainer2.setSpecialization(trainingType);
        trainerRepository.createTrainer(trainer2);

        List<Trainer> found = trainerRepository.findByUsernames(List.of("Tom.Trainer", "Jane.Coach"));

        assertThat(found).hasSize(2);
    }

    @Test
    @Order(602)
    void findByUsernames_withPartialMatch_shouldReturnOnlyMatching() {
        trainerRepository.createTrainer(trainer);

        List<Trainer> found = trainerRepository.findByUsernames(List.of("Tom.Trainer", "Non.Existent"));

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getUser().getUsername()).isEqualTo("Tom.Trainer");
    }

    @Test
    @Order(603)
    void findByUsernames_withNullList_shouldReturnEmptyList() {
        List<Trainer> found = trainerRepository.findByUsernames(null);

        assertThat(found).isEmpty();
    }

    @Test
    @Order(604)
    void findByUsernames_withEmptyList_shouldReturnEmptyList() {
        List<Trainer> found = trainerRepository.findByUsernames(List.of());

        assertThat(found).isEmpty();
    }

    @Test
    @Order(605)
    void findByUsernames_withNoMatches_shouldReturnEmptyList() {
        trainerRepository.createTrainer(trainer);

        List<Trainer> found = trainerRepository.findByUsernames(List.of("Non.Existent"));

        assertThat(found).isEmpty();
    }
}
