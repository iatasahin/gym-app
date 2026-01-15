package dev.ilkersahin.java.spring.gym.storage;

import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class TrainingMapStorageTest {
    private TrainingMapStorage storage;

    private final UUID traineeId = UUID.randomUUID();
    private final UUID trainerId = UUID.randomUUID();
    private final UUID differentTraineeId = UUID.randomUUID();
    private final UUID differentTrainerId = UUID.randomUUID();
    private final LocalDate trainingDate = LocalDate.of(2025, 1, 1);
    private final LocalDate differentDate = LocalDate.of(2025, 1, 2);

    @BeforeEach
    void setUp() {
        storage = new TrainingMapStorage();
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

    // === CREATE TRAINING TESTS ===

    @Test
    void createTraining_withValidTraining_shouldStoreAndReturnTraining() {
        Training training = sample();

        Training result = storage.createTraining(training);
        assertThat(result).isEqualTo(training);

        List<Training> stored = storage.getTraining(traineeId, trainerId, trainingDate);
        assertThat(stored)
                .hasSize(1)
                .contains(training);
    }

    @Test
    void createTraining_withMultipleTrainingsSameKey_shouldStoreAllTrainings() {
        Training training1 = sample("Morning Workout", TrainingType.FITNESS, Duration.ofMinutes(60));
        Training training2 = sample("Evening Workout", TrainingType.FITNESS, Duration.ofMinutes(45));

        storage.createTraining(training1);
        storage.createTraining(training2);

        List<Training> result = storage.getTraining(traineeId, trainerId, trainingDate);
        assertThat(result)
                .hasSize(2)
                .containsExactly(training1, training2);
    }

    @Test
    void createTraining_withDifferentKeys_shouldStoreSeparately() {
        Training training1 = sample(traineeId, trainerId, trainingDate);
        Training training2 = sample(differentTraineeId, trainerId, trainingDate);
        Training training3 = sample(traineeId, differentTrainerId, trainingDate);
        Training training4 = sample(traineeId, trainerId, differentDate);

        storage.createTraining(training1);
        storage.createTraining(training2);
        storage.createTraining(training3);
        storage.createTraining(training4);

        assertThat(storage.getTraining(traineeId, trainerId, trainingDate))
                .hasSize(1)
                .contains(training1);
        assertThat(storage.getTraining(differentTraineeId, trainerId, trainingDate))
                .hasSize(1)
                .contains(training2);
        assertThat(storage.getTraining(traineeId, differentTrainerId, trainingDate))
                .hasSize(1)
                .contains(training3);
        assertThat(storage.getTraining(traineeId, trainerId, differentDate))
                .hasSize(1)
                .contains(training4);
    }

    @Test
    void createTraining_withNullTraineeId_shouldThrowException() {
        assertThatThrownBy(() -> storage.createTraining(new Training(
                null,
                trainerId,
                "Workout",
                TrainingType.FITNESS,
                trainingDate,
                Duration.ofMinutes(60)
        ))).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createTraining_withNullTrainerId_shouldThrowException() {
        assertThatThrownBy(() -> storage.createTraining(new Training(
                traineeId,
                null,
                "Workout",
                TrainingType.FITNESS,
                trainingDate,
                Duration.ofMinutes(60)
        ))).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createTraining_withNullTrainingDate_shouldThrowException() {
        assertThatThrownBy(() -> storage.createTraining(new Training(
                traineeId,
                trainerId,
                "Workout",
                TrainingType.FITNESS,
                null,
                Duration.ofMinutes(60)
        ))).isInstanceOf(NullPointerException.class);
    }

    // === GET TRAINING TESTS ===

    @Test
    void getTraining_withExistingKey_shouldReturnAllTrainingsForKey() {
        Training training = sample();
        storage.createTraining(training);

        List<Training> result = storage.getTraining(traineeId, trainerId, trainingDate);

        assertThat(result)
                .hasSize(1)
                .contains(training);
    }

    @Test
    void getTraining_withNonExistentKey_shouldReturnEmptyList() {
        List<Training> result = storage.getTraining(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2025, 12, 31)
        );

        assertThat(result).isEmpty();
    }

    @Test
    void getTraining_withNullTraineeId_shouldReturnEmptyList() {
        storage.createTraining(sample());

        List<Training> result = storage.getTraining(null, trainerId, trainingDate);

        assertThat(result).isEmpty();
    }

    @Test
    void getTraining_withNullTrainerId_shouldReturnEmptyList() {
        storage.createTraining(sample());

        List<Training> result = storage.getTraining(traineeId, null, trainingDate);

        assertThat(result).isEmpty();
    }

    @Test
    void getTraining_withNullTrainingDate_shouldReturnEmptyList() {
        storage.createTraining(sample());

        List<Training> result = storage.getTraining(traineeId, trainerId, null);

        assertThat(result).isEmpty();
    }

    @Test
    void getTraining_withPartialKeyMatch_shouldReturnEmptyList() {
        storage.createTraining(sample(traineeId, trainerId, trainingDate));

        // Different trainee, same trainer and date
        assertThat(storage.getTraining(differentTraineeId, trainerId, trainingDate)).isEmpty();

        // Same trainee, different trainer, same date
        assertThat(storage.getTraining(traineeId, differentTrainerId, trainingDate)).isEmpty();

        // Same trainee and trainer, different date
        assertThat(storage.getTraining(traineeId, trainerId, differentDate)).isEmpty();
    }

    // === GET ALL TRAININGS TESTS ===

    @Test
    void getAllTrainings_withEmptyStorage_shouldReturnEmptyList() {
        List<Training> result = storage.getAllTrainings();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllTrainings_withSingleTraining_shouldReturnSingleTraining() {
        Training training = sample();
        storage.createTraining(training);

        List<Training> result = storage.getAllTrainings();

        assertThat(result)
                .hasSize(1)
                .contains(training);
    }

    @Test
    void getAllTrainings_withMultipleTrainingsSameKey_shouldReturnAllTrainings() {
        Training training1 = sample("Morning Workout", TrainingType.FITNESS, Duration.ofMinutes(60));
        Training training2 = sample("Evening Workout", TrainingType.STRETCHING, Duration.ofMinutes(45));

        storage.createTraining(training1);
        storage.createTraining(training2);

        List<Training> result = storage.getAllTrainings();

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(training1, training2);
    }

    @Test
    void getAllTrainings_withMultipleTrainingsDifferentKeys_shouldReturnAllTrainings() {
        Training training1 = sample(traineeId, trainerId, trainingDate);
        Training training2 = sample(differentTraineeId, trainerId, trainingDate);
        Training training3 = sample(traineeId, differentTrainerId, differentDate);

        storage.createTraining(training1);
        storage.createTraining(training2);
        storage.createTraining(training3);

        List<Training> result = storage.getAllTrainings();

        assertThat(result)
                .hasSize(3)
                .containsExactlyInAnyOrder(training1, training2, training3);
    }

    @Test
    void getAllTrainings_withMixedKeysAndMultipleTrainings_shouldReturnAllTrainings() {
        // Same key, multiple trainings
        Training training1 = sample("Workout 1", TrainingType.FITNESS, Duration.ofMinutes(30));
        Training training2 = sample("Workout 2", TrainingType.FITNESS, Duration.ofMinutes(45));

        // Different key, single training
        Training training3 = sample(differentTraineeId, trainerId, trainingDate);

        storage.createTraining(training1);
        storage.createTraining(training2);
        storage.createTraining(training3);

        List<Training> result = storage.getAllTrainings();

        assertThat(result)
                .hasSize(3)
                .containsExactlyInAnyOrder(training1, training2, training3);
    }

    // === INTEGRATION TESTS ===

    @Test
    void createAndRetrieve_shouldMaintainDataConsistency() {
        // Create trainings with different keys
        Training training1 = sample(traineeId, trainerId, trainingDate);
        Training training2 = sample(traineeId, trainerId, trainingDate); // Same key
        Training training3 = sample(differentTraineeId, trainerId, trainingDate); // Different key

        storage.createTraining(training1);
        storage.createTraining(training2);
        storage.createTraining(training3);

        // Verify specific key retrieval
        List<Training> sameKeyTrainings = storage.getTraining(traineeId, trainerId, trainingDate);
        assertThat(sameKeyTrainings)
                .hasSize(2)
                .containsExactly(training1, training2);

        List<Training> differentKeyTrainings = storage.getTraining(differentTraineeId, trainerId, trainingDate);
        assertThat(differentKeyTrainings)
                .hasSize(1)
                .contains(training3);

        // Verify all trainings retrieval
        List<Training> allTrainings = storage.getAllTrainings();
        assertThat(allTrainings)
                .hasSize(3)
                .containsExactlyInAnyOrder(training1, training2, training3);
    }

    @Test
    void trainingKeyEquality_shouldWorkCorrectly() {
        Training training1 = sample(traineeId, trainerId, trainingDate);
        Training training2 = sample(traineeId, trainerId, trainingDate);

        storage.createTraining(training1);
        storage.createTraining(training2);

        // Both trainings should be stored under the same key
        List<Training> trainings = storage.getTraining(traineeId, trainerId, trainingDate);
        assertThat(trainings)
                .hasSize(2)
                .containsExactly(training1, training2);
    }

}
