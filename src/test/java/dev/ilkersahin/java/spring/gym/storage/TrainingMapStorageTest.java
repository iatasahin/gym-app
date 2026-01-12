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
    private final LocalDate date = LocalDate.of(2025, 1, 1);

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
                date,
                Duration.ofMinutes(60)
        );
    }

    @Test
    void storeAndRetrieveByKey() {
        storage.createTraining(sample());

        List<Training> found =
                storage.getTraining(traineeId, trainerId, date);

        assertThat(found).hasSize(1);
    }

    @Test
    void multipleTrainingsSameKey() {
        storage.createTraining(sample());
        storage.createTraining(sample());

        assertThat(storage.getTraining(traineeId, trainerId, date))
                .hasSize(2);
    }

    @Test
    void getAllTrainings() {
        storage.createTraining(sample());

        assertThat(storage.getAllTrainings()).hasSize(1);
    }

    @Test
    void wrongKeyReturnsEmpty() {
        assertThat(storage
                .getTraining(UUID.randomUUID(), UUID.randomUUID(), date))
                .isEmpty();
    }
}
