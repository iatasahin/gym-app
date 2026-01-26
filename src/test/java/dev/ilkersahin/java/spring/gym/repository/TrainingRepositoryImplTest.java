package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.config.TestPersistenceConfig;
import dev.ilkersahin.java.spring.gym.dao.TraineeDao;
import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.dao.TrainingDao;
import dev.ilkersahin.java.spring.gym.dao.UserDao;
import dev.ilkersahin.java.spring.gym.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPersistenceConfig.class)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TrainingRepositoryImplTest {
    @Autowired
    private TrainingDao trainingRepository;

    @Autowired
    private TraineeDao traineeRepository;

    @Autowired
    private TrainerDao trainerRepository;

    @Autowired
    private UserDao userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;
    private Training training;

    @BeforeEach
    void setUp() {
        // Create training type
        trainingType = TrainingType.fromEnum(TrainingType.Type.FITNESS);
        entityManager.persist(trainingType);

        // Create trainee
        User traineeUser = new User("Jack", "Black", "Jack.Black", "pass", true);
        userRepository.persist(traineeUser);
        trainee = new Trainee();
        trainee.setUser(traineeUser);
        traineeRepository.createTrainee(trainee);

        // Create trainer
        User trainerUser = new User("Tom", "Trainer", "Tom.Trainer", "pass", true);
        userRepository.persist(trainerUser);
        trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(trainingType);
        trainerRepository.createTrainer(trainer);

        // Create training
        training = new Training(
                trainee,
                trainer,
                "Morning Fitness",
                trainingType,
                LocalDate.of(2024, 6, 15),
                Duration.ofMinutes(60)
        );
    }

    // =========================================================================
    // CREATE TRAINING TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void createTraining_withValidTraining_shouldPersistAndReturn() {
        Training created = trainingRepository.createTraining(training);

        assertThat(created).isNotNull();
        assertThat(created.getTrainingId()).isNotNull();
        assertThat(created.getTrainingName()).isEqualTo("Morning Fitness");
    }

    @Test
    @Order(102)
    void createTraining_withValidTraining_shouldBeRetrievable() {
        trainingRepository.createTraining(training);

        List<Training> all = trainingRepository.getAllTrainings();

        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getTrainingName()).isEqualTo("Morning Fitness");
    }

    // =========================================================================
    // GET TRAINING BY IDS TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void getTraining_withValidIds_shouldReturnTraining() {
        trainingRepository.createTraining(training);

        List<Training> found = trainingRepository.getTraining(
                trainee.getTraineeId(),
                trainer.getTrainerId(),
                LocalDate.of(2024, 6, 15)
        );

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getTrainingName()).isEqualTo("Morning Fitness");
    }

    @Test
    @Order(202)
    void getTraining_withNullTraineeId_shouldReturnEmptyList() {
        List<Training> found = trainingRepository.getTraining(
                null,
                trainer.getTrainerId(),
                LocalDate.of(2024, 6, 15)
        );

        assertThat(found).isEmpty();
    }

    @Test
    @Order(203)
    void getTraining_withNullTrainerId_shouldReturnEmptyList() {
        List<Training> found = trainingRepository.getTraining(
                trainee.getTraineeId(),
                null,
                LocalDate.of(2024, 6, 15)
        );

        assertThat(found).isEmpty();
    }

    @Test
    @Order(204)
    void getTraining_withNullDate_shouldReturnEmptyList() {
        List<Training> found = trainingRepository.getTraining(
                trainee.getTraineeId(),
                trainer.getTrainerId(),
                null
        );

        assertThat(found).isEmpty();
    }

    @Test
    @Order(205)
    void getTraining_withNonMatchingIds_shouldReturnEmptyList() {
        trainingRepository.createTraining(training);

        List<Training> found = trainingRepository.getTraining(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2024, 6, 15)
        );

        assertThat(found).isEmpty();
    }

    // =========================================================================
    // GET ALL TRAININGS TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void getAllTrainings_withNoTrainings_shouldReturnEmptyList() {
        List<Training> trainings = trainingRepository.getAllTrainings();

        assertThat(trainings).isEmpty();
    }

    @Test
    @Order(302)
    void getAllTrainings_withMultipleTrainings_shouldReturnAllOrderedByDateDesc() {
        trainingRepository.createTraining(training);

        Training training2 = new Training(
                trainee, trainer, "Evening Session", trainingType,
                LocalDate.of(2024, 6, 20), Duration.ofMinutes(45)
        );
        trainingRepository.createTraining(training2);

        List<Training> trainings = trainingRepository.getAllTrainings();

        assertThat(trainings).hasSize(2);
        assertThat(trainings.get(0).getTrainingDate()).isEqualTo(LocalDate.of(2024, 6, 20));
        assertThat(trainings.get(1).getTrainingDate()).isEqualTo(LocalDate.of(2024, 6, 15));
    }

    // =========================================================================
    // FIND FOR TRAINEE TESTS (400s) - Branch coverage
    // =========================================================================

    @Test
    @Order(401)
    void findForTrainee_withNoFilters_shouldReturnAllForTrainee() {
        trainingRepository.createTraining(training);

        List<Training> found = trainingRepository.findForTrainee(
                "Jack.Black", null, null, null, null
        );

        assertThat(found).hasSize(1);
    }

    @Test
    @Order(402)
    void findForTrainee_withFromDate_shouldFilterByFromDate() {
        trainingRepository.createTraining(training);

        Training earlier = new Training(
                trainee, trainer, "Earlier", trainingType,
                LocalDate.of(2024, 5, 1), Duration.ofMinutes(30)
        );
        trainingRepository.createTraining(earlier);

        List<Training> found = trainingRepository.findForTrainee(
                "Jack.Black", LocalDate.of(2024, 6, 1), null, null, null
        );

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getTrainingName()).isEqualTo("Morning Fitness");
    }

    @Test
    @Order(403)
    void findForTrainee_withToDate_shouldFilterByToDate() {
        trainingRepository.createTraining(training);

        Training later = new Training(
                trainee, trainer, "Later", trainingType,
                LocalDate.of(2024, 7, 1), Duration.ofMinutes(30)
        );
        trainingRepository.createTraining(later);

        List<Training> found = trainingRepository.findForTrainee(
                "Jack.Black", null, LocalDate.of(2024, 6, 30), null, null
        );

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getTrainingName()).isEqualTo("Morning Fitness");
    }

    @Test
    @Order(404)
    void findForTrainee_withTrainerUsername_shouldFilterByTrainer() {
        trainingRepository.createTraining(training);

        // Create another trainer and training
        User otherTrainerUser = new User("Jane", "Coach", "Jane.Coach", "pass", true);
        userRepository.persist(otherTrainerUser);
        Trainer otherTrainer = new Trainer();
        otherTrainer.setUser(otherTrainerUser);
        otherTrainer.setSpecialization(trainingType);
        trainerRepository.createTrainer(otherTrainer);

        Training otherTraining = new Training(
                trainee, otherTrainer, "Other Training", trainingType,
                LocalDate.of(2024, 6, 15), Duration.ofMinutes(30)
        );
        trainingRepository.createTraining(otherTraining);

        List<Training> found = trainingRepository.findForTrainee(
                "Jack.Black", null, null, "Tom.Trainer", null
        );

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getTrainer().getUser().getUsername()).isEqualTo("Tom.Trainer");
    }

    @Test
    @Order(405)
    void findForTrainee_withTrainingType_shouldFilterByType() {
        trainingRepository.createTraining(training);

        // Create yoga training type and training
        TrainingType yoga = TrainingType.fromEnum(TrainingType.Type.YOGA);
        entityManager.persist(yoga);

        Training yogaTraining = new Training(
                trainee, trainer, "Yoga Session", yoga,
                LocalDate.of(2024, 6, 15), Duration.ofMinutes(45)
        );
        trainingRepository.createTraining(yogaTraining);

        List<Training> found = trainingRepository.findForTrainee(
                "Jack.Black", null, null, null, TrainingType.Type.FITNESS
        );

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getTrainingName()).isEqualTo("Morning Fitness");
    }

    @Test
    @Order(406)
    void findForTrainee_withAllFilters_shouldApplyAllFilters() {
        trainingRepository.createTraining(training);

        List<Training> found = trainingRepository.findForTrainee(
                "Jack.Black",
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 6, 30),
                "Tom.Trainer",
                TrainingType.Type.FITNESS
        );

        assertThat(found).hasSize(1);
    }

    @Test
    @Order(407)
    void findForTrainee_withBlankTrainerUsername_shouldIgnoreTrainerFilter() {
        trainingRepository.createTraining(training);

        List<Training> found = trainingRepository.findForTrainee(
                "Jack.Black", null, null, "   ", null
        );

        assertThat(found).hasSize(1);
    }

    // =========================================================================
    // FIND FOR TRAINER TESTS (500s) - Branch coverage
    // =========================================================================

    @Test
    @Order(501)
    void findForTrainer_withNoFilters_shouldReturnAllForTrainer() {
        trainingRepository.createTraining(training);

        List<Training> found = trainingRepository.findForTrainer(
                "Tom.Trainer", null, null, null
        );

        assertThat(found).hasSize(1);
    }

    @Test
    @Order(502)
    void findForTrainer_withFromDate_shouldFilterByFromDate() {
        trainingRepository.createTraining(training);

        Training earlier = new Training(
                trainee, trainer, "Earlier", trainingType,
                LocalDate.of(2024, 5, 1), Duration.ofMinutes(30)
        );
        trainingRepository.createTraining(earlier);

        List<Training> found = trainingRepository.findForTrainer(
                "Tom.Trainer", LocalDate.of(2024, 6, 1), null, null
        );

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTrainingName()).isEqualTo("Morning Fitness");
    }

    @Test
    @Order(503)
    void findForTrainer_withToDate_shouldFilterByToDate() {
        trainingRepository.createTraining(training);

        Training later = new Training(
                trainee, trainer, "Later", trainingType,
                LocalDate.of(2024, 7, 1), Duration.ofMinutes(30)
        );
        trainingRepository.createTraining(later);

        List<Training> found = trainingRepository.findForTrainer(
                "Tom.Trainer", null, LocalDate.of(2024, 6, 30), null
        );

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getTrainingName()).isEqualTo("Morning Fitness");
    }

    @Test
    @Order(504)
    void findForTrainer_withTraineeUsername_shouldFilterByTrainee() {
        trainingRepository.createTraining(training);

        // Create another trainee and training
        User otherTraineeUser = new User("Bob", "Student", "Bob.Student", "pass", true);
        userRepository.persist(otherTraineeUser);
        Trainee otherTrainee = new Trainee();
        otherTrainee.setUser(otherTraineeUser);
        traineeRepository.createTrainee(otherTrainee);

        Training otherTraining = new Training(
                otherTrainee, trainer, "Other Training", trainingType,
                LocalDate.of(2024, 6, 15), Duration.ofMinutes(30)
        );
        trainingRepository.createTraining(otherTraining);

        List<Training> found = trainingRepository.findForTrainer(
                "Tom.Trainer", null, null, "Jack.Black"
        );

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getTrainee().getUser().getUsername()).isEqualTo("Jack.Black");
    }

    @Test
    @Order(505)
    void findForTrainer_withAllFilters_shouldApplyAllFilters() {
        trainingRepository.createTraining(training);

        List<Training> found = trainingRepository.findForTrainer(
                "Tom.Trainer",
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 6, 30),
                "Jack.Black"
        );

        assertThat(found).hasSize(1);
    }

    @Test
    @Order(506)
    void findForTrainer_withBlankTraineeUsername_shouldIgnoreTraineeFilter() {
        trainingRepository.createTraining(training);

        List<Training> found = trainingRepository.findForTrainer(
                "Tom.Trainer", null, null, "   "
        );

        assertThat(found).hasSize(1);
    }
}
