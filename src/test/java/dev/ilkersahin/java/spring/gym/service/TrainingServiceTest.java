package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TrainingDAO;
import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TrainingServiceTest {

    private TrainingService service;
    private TrainingDAO trainingDAO;

    private final UUID traineeId = UUID.randomUUID();
    private final UUID trainerId = UUID.randomUUID();
    private final UUID differentTraineeId = UUID.randomUUID();
    private final UUID differentTrainerId = UUID.randomUUID();
    private final LocalDate trainingDate = LocalDate.of(2024, 8, 24);
    private final LocalDate differentDate = LocalDate.of(2024, 8, 25);


    @BeforeEach
    void setUp() {
        trainingDAO = mock(TrainingDAO.class);
        service = new TrainingService();
        service.setTrainingDAO(trainingDAO);
    }

    private Training sample() {
        return new Training(
                traineeId,
                trainerId,
                "Workout",
                TrainingType.FITNESS,
                trainingDate,
                Duration.ofMinutes(60)
        );
    }

    private Training sample(UUID traineeId, UUID trainerId, LocalDate date) {
        return new Training(
                traineeId,
                trainerId,
                "Custom Workout",
                TrainingType.FITNESS,
                date,
                Duration.ofMinutes(45)
        );
    }

    private Training sample(String name, TrainingType type, Duration duration) {
        return new Training(
                traineeId,
                trainerId,
                name,
                type,
                trainingDate,
                duration
        );
    }

    // === CREATE TRAINING TESTS (101+) ===

    @Test
    @Order(101)
    void createTraining_withValidTraining_shouldDelegateToDAOAndReturnTraining() {
        Training training = sample();
        when(trainingDAO.createTraining(training)).thenReturn(training);

        Training result = service.createTraining(training);

        assertThat(result).isSameAs(training);
        verify(trainingDAO).createTraining(training);
    }

    @Test
    @Order(102)
    void createTraining_withDifferentTrainingTypes_shouldDelegateToDAO() {
        Training fitnessTraining = sample("Fitness Session", TrainingType.FITNESS, Duration.ofMinutes(60));
        Training resistanceTraining = sample("Cardio Session", TrainingType.RESISTANCE, Duration.ofMinutes(45));

        when(trainingDAO.createTraining(fitnessTraining)).thenReturn(fitnessTraining);
        when(trainingDAO.createTraining(resistanceTraining)).thenReturn(resistanceTraining);

        Training fitnessResult = service.createTraining(fitnessTraining);
        Training resistanceResult = service.createTraining(resistanceTraining);

        assertThat(fitnessResult.getTrainingType()).isEqualTo(TrainingType.FITNESS);
        assertThat(resistanceResult.getTrainingType()).isEqualTo(TrainingType.RESISTANCE);
        verify(trainingDAO).createTraining(fitnessTraining);
        verify(trainingDAO).createTraining(resistanceTraining);
    }

    @Test
    @Order(103)
    void createTraining_withDifferentDurations_shouldDelegateToDAO() {
        Training shortTraining = sample("Short Workout", TrainingType.FITNESS, Duration.ofMinutes(30));
        Training longTraining = sample("Long Workout", TrainingType.FITNESS, Duration.ofMinutes(120));

        when(trainingDAO.createTraining(shortTraining)).thenReturn(shortTraining);
        when(trainingDAO.createTraining(longTraining)).thenReturn(longTraining);

        Training shortResult = service.createTraining(shortTraining);
        Training longResult = service.createTraining(longTraining);

        assertThat(shortResult.getTrainingDuration()).isEqualTo(Duration.ofMinutes(30));
        assertThat(longResult.getTrainingDuration()).isEqualTo(Duration.ofMinutes(120));
    }

    @Test
    @Order(104)
    void createTraining_withNullTraining_shouldThrowException() {
        assertThatThrownBy(() -> service.createTraining(null))
                .isInstanceOf(NullPointerException.class);

        verify(trainingDAO, never()).createTraining(any());
    }

    // === GET TRAINING TESTS ===

    @Test
    @Order(201)
    void getTraining_withValidParameters_shouldReturnTrainingsFromDAO() {
        Training training = sample();
        when(trainingDAO.getTraining(traineeId, trainerId, trainingDate))
                .thenReturn(List.of(training));

        List<Training> result = service.getTraining(
                traineeId, trainerId, trainingDate
        );

        assertThat(result)
                .hasSize(1)
                .contains(training);
        verify(trainingDAO).getTraining(traineeId, trainerId, trainingDate);
    }

    @Test
    @Order(202)
    void getTraining_withMultipleTrainingsSameKey_shouldReturnAllTrainings() {
        Training training1 = sample("Morning Workout", TrainingType.FITNESS, Duration.ofMinutes(60));
        Training training2 = sample("Evening Workout", TrainingType.STRETCHING, Duration.ofMinutes(45));

        when(trainingDAO.getTraining(traineeId, trainerId, trainingDate))
                .thenReturn(List.of(training1, training2));

        List<Training> result = service.getTraining(traineeId, trainerId, trainingDate);

        assertThat(result)
                .hasSize(2)
                .containsExactly(training1, training2);
    }

    @Test
    @Order(203)
    void getTraining_withNonExistentKey_shouldReturnEmptyList() {
        when(trainingDAO.getTraining(differentTraineeId, differentTrainerId, differentDate))
                .thenReturn(List.of());

        List<Training> result = service.getTraining(differentTraineeId, differentTrainerId, differentDate);

        assertThat(result).isEmpty();
        verify(trainingDAO).getTraining(differentTraineeId, differentTrainerId, differentDate);
    }

    @Test
    @Order(204)
    void getTraining_withNullTraineeId_shouldDelegateToDAO() {
        when(trainingDAO.getTraining(null, trainerId, trainingDate))
                .thenReturn(List.of());

        List<Training> result = service.getTraining(null, trainerId, trainingDate);

        assertThat(result).isEmpty();
        verify(trainingDAO).getTraining(null, trainerId, trainingDate);
    }

    @Test
    @Order(205)
    void getTraining_withNullTrainerId_shouldDelegateToDAO() {
        when(trainingDAO.getTraining(traineeId, null, trainingDate))
                .thenReturn(List.of());

        List<Training> result = service.getTraining(traineeId, null, trainingDate);

        assertThat(result).isEmpty();
        verify(trainingDAO).getTraining(traineeId, null, trainingDate);
    }

    @Test
    @Order(206)
    void getTraining_withNullTrainingDate_shouldDelegateToDAO() {
        when(trainingDAO.getTraining(traineeId, trainerId, null))
                .thenReturn(List.of());

        List<Training> result = service.getTraining(traineeId, trainerId, null);

        assertThat(result).isEmpty();
        verify(trainingDAO).getTraining(traineeId, trainerId, null);
    }

    // === GET ALL TRAININGS TESTS ===

    @Test
    @Order(501)
    void getAllTrainings_withExistingTrainings_shouldReturnAllTrainings() {
        Training training1 = sample();
        Training training2 = sample(differentTraineeId, trainerId, trainingDate);

        when(trainingDAO.getAllTrainings()).thenReturn(List.of(training1, training2));

        List<Training> result = service.getAllTrainings();

        assertThat(result)
                .hasSize(2)
                .containsExactly(training1, training2);
        verify(trainingDAO).getAllTrainings();
    }

    @Test
    @Order(502)
    void getAllTrainings_withEmptyRepository_shouldReturnEmptyList() {
        when(trainingDAO.getAllTrainings()).thenReturn(List.of());

        List<Training> result = service.getAllTrainings();

        assertThat(result).isEmpty();
        verify(trainingDAO).getAllTrainings();
    }

    @Test
    @Order(503)
    void getAllTrainings_shouldPreserveTrainingOrder() {
        Training training1 = sample("First Training", TrainingType.FITNESS, Duration.ofMinutes(30));
        Training training2 = sample("Second Training", TrainingType.ZUMBA, Duration.ofMinutes(45));
        Training training3 = sample("Third Training", TrainingType.YOGA, Duration.ofMinutes(60));

        when(trainingDAO.getAllTrainings()).thenReturn(List.of(training1, training2, training3));

        List<Training> result = service.getAllTrainings();

        assertThat(result).containsExactly(training1, training2, training3);
    }

    @Test
    @Order(504)
    void getAllTrainings_withDifferentTrainingTypes_shouldReturnAllTypes() {
        Training fitnessTraining = sample("Fitness", TrainingType.FITNESS, Duration.ofMinutes(60));
        Training cardioTraining = sample("Resistance", TrainingType.RESISTANCE, Duration.ofMinutes(45));
        Training yogaTraining = sample("Yoga", TrainingType.YOGA, Duration.ofMinutes(90));

        when(trainingDAO.getAllTrainings()).thenReturn(List.of(fitnessTraining, cardioTraining, yogaTraining));

        List<Training> result = service.getAllTrainings();

        assertThat(result)
                .hasSize(3)
                .extracting(Training::getTrainingType)
                .containsExactly(TrainingType.FITNESS, TrainingType.RESISTANCE, TrainingType.YOGA);
    }

    // === INTEGRATION TESTS ===

    @Test
    @Order(601)
    void createAndRetrieveTraining_shouldMaintainDataConsistency() {
        Training training = sample();

        // Setup create operation
        when(trainingDAO.createTraining(training)).thenReturn(training);

        // Setup get operation
        when(trainingDAO.getTraining(traineeId, trainerId, trainingDate))
                .thenReturn(List.of(training));

        // Execute operations
        Training created = service.createTraining(training);
        List<Training> retrieved = service.getTraining(traineeId, trainerId, trainingDate);

        // Verify consistency
        assertThat(created).isSameAs(training);
        assertThat(retrieved)
                .hasSize(1)
                .contains(training);
    }

    @Test
    @Order(602)
    void createMultipleAndRetrieveAll_shouldMaintainDataConsistency() {
        Training training1 = sample("Training 1", TrainingType.FITNESS, Duration.ofMinutes(60));
        Training training2 = sample(differentTraineeId, trainerId, differentDate);

        // Setup create operations
        when(trainingDAO.createTraining(training1)).thenReturn(training1);
        when(trainingDAO.createTraining(training2)).thenReturn(training2);

        // Setup get all operation
        when(trainingDAO.getAllTrainings()).thenReturn(List.of(training1, training2));

        // Execute operations
        Training created1 = service.createTraining(training1);
        Training created2 = service.createTraining(training2);
        List<Training> allTrainings = service.getAllTrainings();

        // Verify consistency
        assertThat(created1).isSameAs(training1);
        assertThat(created2).isSameAs(training2);
        assertThat(allTrainings)
                .hasSize(2)
                .containsExactly(training1, training2);
    }

    @Test
    @Order(603)
    void serviceOperations_shouldHandleComplexScenarios() {
        // Create trainings with same key
        Training morning = sample("Morning Session", TrainingType.FITNESS, Duration.ofMinutes(60));
        Training evening = sample("Evening Session", TrainingType.RESISTANCE, Duration.ofMinutes(45));

        // Create training with different key
        Training differentDay = sample(traineeId, trainerId, differentDate);

        // Setup mocks
        when(trainingDAO.createTraining(morning)).thenReturn(morning);
        when(trainingDAO.createTraining(evening)).thenReturn(evening);
        when(trainingDAO.createTraining(differentDay)).thenReturn(differentDay);

        when(trainingDAO.getTraining(traineeId, trainerId, trainingDate))
                .thenReturn(List.of(morning, evening));
        when(trainingDAO.getTraining(traineeId, trainerId, differentDate))
                .thenReturn(List.of(differentDay));
        when(trainingDAO.getAllTrainings())
                .thenReturn(List.of(morning, evening, differentDay));

        // Execute operations
        service.createTraining(morning);
        service.createTraining(evening);
        service.createTraining(differentDay);

        List<Training> sameKeyTrainings = service.getTraining(traineeId, trainerId, trainingDate);
        List<Training> differentKeyTrainings = service.getTraining(traineeId, trainerId, differentDate);
        List<Training> allTrainings = service.getAllTrainings();

        // Verify results
        assertThat(sameKeyTrainings)
                .hasSize(2)
                .containsExactly(morning, evening);
        assertThat(differentKeyTrainings)
                .hasSize(1)
                .contains(differentDay);
        assertThat(allTrainings)
                .hasSize(3)
                .containsExactly(morning, evening, differentDay);
    }

    @Test
    @Order(604)
    void dependencyInjection_shouldWorkCorrectly() {
        TrainingService newService = new TrainingService();
        TrainingDAO mockDAO = mock(TrainingDAO.class);

        newService.setTrainingDAO(mockDAO);

        // Verify DAO injection
        when(mockDAO.getAllTrainings()).thenReturn(List.of());
        List<Training> result = newService.getAllTrainings();
        assertThat(result).isEmpty();
        verify(mockDAO).getAllTrainings();

        // Verify create operation
        Training training = sample();
        when(mockDAO.createTraining(training)).thenReturn(training);
        Training created = newService.createTraining(training);
        assertThat(created).isSameAs(training);
        verify(mockDAO).createTraining(training);
    }

    @Test
    @Order(605)
    void trainingKeyVariations_shouldHandleAllCombinations() {
        Training baseTraining = sample();
        Training trainingDifferentTrainee = sample(differentTraineeId, trainerId, trainingDate);
        Training trainingDifferentTrainer = sample(traineeId, differentTrainerId, trainingDate);
        Training trainingDifferentDate = sample(traineeId, trainerId, differentDate);

        // Setup mocks for each key combination
        when(trainingDAO.getTraining(traineeId, trainerId, trainingDate))
                .thenReturn(List.of(baseTraining));
        when(trainingDAO.getTraining(differentTraineeId, trainerId, trainingDate))
                .thenReturn(List.of(trainingDifferentTrainee));
        when(trainingDAO.getTraining(traineeId, differentTrainerId, trainingDate))
                .thenReturn(List.of(trainingDifferentTrainer));
        when(trainingDAO.getTraining(traineeId, trainerId, differentDate))
                .thenReturn(List.of(trainingDifferentDate));

        // Test each key combination
        assertThat(service.getTraining(traineeId, trainerId, trainingDate))
                .containsExactly(baseTraining);
        assertThat(service.getTraining(differentTraineeId, trainerId, trainingDate))
                .containsExactly(trainingDifferentTrainee);
        assertThat(service.getTraining(traineeId, differentTrainerId, trainingDate))
                .containsExactly(trainingDifferentTrainer);
        assertThat(service.getTraining(traineeId, trainerId, differentDate))
                .containsExactly(trainingDifferentDate);

        // Verify all calls were made
        verify(trainingDAO).getTraining(traineeId, trainerId, trainingDate);
        verify(trainingDAO).getTraining(differentTraineeId, trainerId, trainingDate);
        verify(trainingDAO).getTraining(traineeId, differentTrainerId, trainingDate);
        verify(trainingDAO).getTraining(traineeId, trainerId, differentDate);
    }
}
