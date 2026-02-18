package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TrainingRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private UserRepository userRepository;

    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        // Create trainee
        User traineeUser = new User("Trainee", "Test", "Trainee.Test." + System.nanoTime(), "pass", true);
        userRepository.save(traineeUser);
        trainee = new Trainee();
        trainee.setUser(traineeUser);
        traineeRepository.save(trainee);

        // Create trainer
        User trainerUser = new User("Trainer", "Test", "Trainer.Test." + System.nanoTime(), "pass", true);
        userRepository.save(trainerUser);
        trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(TrainingType.fromEnum(TrainingType.Type.FITNESS));
        trainerRepository.save(trainer);
    }

    // =========================================================================
    // SAVE TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void save_withValidTraining_shouldPersist() {
        Training training = new Training(
                trainee, trainer, "Morning Workout",
                TrainingType.fromEnum(TrainingType.Type.FITNESS),
                LocalDate.of(2024, 6, 15), 60
        );

        Training saved = trainingRepository.save(training);

        assertThat(saved.getTrainingId()).isNotNull();
        assertThat(saved.getTrainingName()).isEqualTo("Morning Workout");
    }

    // =========================================================================
    // FIND FOR TRAINEE TESTS (200s) - Custom Criteria API
    // =========================================================================

    @Test
    @Order(201)
    void findForTrainee_withNoFilters_shouldReturnAllTrainingsForTrainee() {
        Training training1 = new Training(trainee, trainer, "Session 1",
                TrainingType.fromEnum(TrainingType.Type.FITNESS),
                LocalDate.of(2024, 6, 1), 60);
        Training training2 = new Training(trainee, trainer, "Session 2",
                TrainingType.fromEnum(TrainingType.Type.FITNESS),
                LocalDate.of(2024, 6, 15), 45);

        trainingRepository.save(training1);
        trainingRepository.save(training2);

        List<Training> found = trainingRepository.findForTrainee(
                trainee.getUsername(), null, null, null, null
        );

        assertThat(found).hasSize(2);
    }

    @Test
    @Order(202)
    void findForTrainee_withDateFilter_shouldFilterByDate() {
        Training training1 = new Training(trainee, trainer, "Early Session",
                TrainingType.fromEnum(TrainingType.Type.FITNESS),
                LocalDate.of(2024, 1, 15), 60);
        Training training2 = new Training(trainee, trainer, "Late Session",
                TrainingType.fromEnum(TrainingType.Type.FITNESS),
                LocalDate.of(2024, 12, 15), 60);

        trainingRepository.save(training1);
        trainingRepository.save(training2);

        List<Training> found = trainingRepository.findForTrainee(
                trainee.getUsername(),
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 12, 31),
                null, null
        );

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getTrainingName()).isEqualTo("Late Session");
    }

    @Test
    @Order(203)
    void findForTrainee_withTrainingTypeFilter_shouldFilterByType() {
        Training fitnessTraining = new Training(trainee, trainer, "Fitness",
                TrainingType.fromEnum(TrainingType.Type.FITNESS),
                LocalDate.of(2024, 6, 1), 60);

        // Create yoga trainer for yoga training
        User yogaTrainerUser = new User("Yoga", "Trainer", "Yoga.Trainer." + System.nanoTime(), "pass", true);
        userRepository.save(yogaTrainerUser);
        Trainer yogaTrainer = new Trainer();
        yogaTrainer.setUser(yogaTrainerUser);
        yogaTrainer.setSpecialization(TrainingType.fromEnum(TrainingType.Type.YOGA));
        trainerRepository.save(yogaTrainer);

        Training yogaTraining = new Training(trainee, yogaTrainer, "Yoga",
                TrainingType.fromEnum(TrainingType.Type.YOGA),
                LocalDate.of(2024, 6, 2), 45);

        trainingRepository.save(fitnessTraining);
        trainingRepository.save(yogaTraining);

        List<Training> found = trainingRepository.findForTrainee(
                trainee.getUsername(), null, null, null, TrainingType.Type.YOGA
        );

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getTrainingName()).isEqualTo("Yoga");
    }

    // =========================================================================
    // FIND FOR TRAINER TESTS (300s) - Custom Criteria API
    // =========================================================================

    @Test
    @Order(301)
    void findForTrainer_withNoFilters_shouldReturnAllTrainingsForTrainer() {
        Training training = new Training(trainee, trainer, "Training Session",
                TrainingType.fromEnum(TrainingType.Type.FITNESS),
                LocalDate.of(2024, 6, 15), 60);

        trainingRepository.save(training);

        List<Training> found = trainingRepository.findForTrainer(
                trainer.getUsername(), null, null, null
        );

        assertThat(found).hasSize(1);
    }

    @Test
    @Order(302)
    void findForTrainer_withTraineeFilter_shouldFilterByTrainee() {
        // Create another trainee
        User otherTraineeUser = new User("Other", "Trainee", "Other.Trainee." + System.nanoTime(), "pass", true);
        userRepository.save(otherTraineeUser);
        Trainee otherTrainee = new Trainee();
        otherTrainee.setUser(otherTraineeUser);
        traineeRepository.save(otherTrainee);

        Training training1 = new Training(trainee, trainer, "Session 1",
                TrainingType.fromEnum(TrainingType.Type.FITNESS),
                LocalDate.of(2024, 6, 1), 60);
        Training training2 = new Training(otherTrainee, trainer, "Session 2",
                TrainingType.fromEnum(TrainingType.Type.FITNESS),
                LocalDate.of(2024, 6, 2), 60);

        trainingRepository.save(training1);
        trainingRepository.save(training2);

        List<Training> found = trainingRepository.findForTrainer(
                trainer.getUsername(), null, null, trainee.getUsername()
        );

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getTrainee().getUsername()).isEqualTo(trainee.getUsername());
    }
}
