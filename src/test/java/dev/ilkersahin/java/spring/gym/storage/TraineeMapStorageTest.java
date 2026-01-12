package dev.ilkersahin.java.spring.gym.storage;

import dev.ilkersahin.java.spring.gym.exception.TraineeDoesNotExistException;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class TraineeMapStorageTest {
    private TraineeMapStorage storage;

    @BeforeEach
    void setUp() {
        storage = new TraineeMapStorage();
    }

    private Trainee sample(String username) {
        Trainee t = new Trainee();
        t.setUsername(username);
        t.setFirstName("Jack");
        t.setLastName("Black");
        t.setDateOfBirth(LocalDate.of(1990, 1, 1));
        t.setAddress("Somewhere");
        t.setUserId(UUID.randomUUID());
        t.setActive(true);
        return t;
    }

    @Test
    void createAndGetTrainee() {
        Trainee t = sample("jack.black");

        storage.createTrainee(t);

        assertThat(storage.getTrainee("jack.black"))
                .isPresent()
                .contains(t);
    }

    @Test
    void creatingDuplicateUsernameThrows() {
        storage.createTrainee(sample("jack.black"));

        assertThatThrownBy(
                () -> storage.createTrainee(sample("jack.black"))
        ).isInstanceOf(UsernameExistsException.class);
    }

    @Test
    void updateExistingTrainee() {
        Trainee t = sample("jack.black");
        storage.createTrainee(t);

        t.setFirstName("Updated");
        Trainee updated = storage.updateTrainee(t);

        assertThat(updated.getFirstName()).isEqualTo("Updated");
    }

    @Test
    void updatingMissingTraineeThrows() {
        assertThatThrownBy(() -> storage.updateTrainee(sample("missing")))
                .isInstanceOf(TraineeDoesNotExistException.class);
    }

    @Test
    void deleteTrainee() {
        Trainee t = sample("jack.black");
        storage.createTrainee(t);

        assertThat(storage.deleteTrainee("jack.black")).isPresent();
        assertThat(storage.getTrainee("jack.black")).isEmpty();
    }

    @Test
    void deleteMissingTraineeNoExceptions() {
        assertThat(storage.getTrainee("missing")).isEmpty();

        assertThat(storage.deleteTrainee("missing")).isEmpty();
        assertThat(storage.getTrainee("missing")).isEmpty();
    }

    @Test
    void getAllTrainees() {
        storage.createTrainee(sample("a"));
        storage.createTrainee(sample("b"));

        assertThat(storage.getAllTrainees()).hasSize(2);
    }
}
