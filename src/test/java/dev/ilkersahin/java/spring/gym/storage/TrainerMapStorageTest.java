package dev.ilkersahin.java.spring.gym.storage;

import dev.ilkersahin.java.spring.gym.exception.TrainerDoesNotExistException;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class TrainerMapStorageTest {
    private TrainerMapStorage storage;

    @BeforeEach
    void setUp() {
        storage = new TrainerMapStorage();
    }

    private Trainer sample(String username) {
        Trainer t = new Trainer();
        t.setUsername(username);
        t.setFirstName("Tom");
        t.setLastName("Smith");
        t.setActive(true);
        t.setUserId(UUID.randomUUID());
        t.setSpecialization(TrainingType.FITNESS);
        return t;
    }

    @Test
    void createAndGetTrainer() {
        Trainer t = sample("tom.smith");
        storage.createTrainer(t);

        assertThat(storage.getTrainer("tom.smith"))
                .isPresent()
                .contains(t);
    }

    @Test
    void creatingDuplicateUsernameThrows() {
        storage.createTrainer(sample("tom.smith"));

        assertThatThrownBy(() -> storage.createTrainer(sample("tom.smith")))
                .isInstanceOf(UsernameExistsException.class);
    }

    @Test
    void updateExistingTrainer() {
        Trainer t = sample("tom.smith");
        storage.createTrainer(t);

        t.setFirstName("Updated");
        Trainer updated = storage.updateTrainer(t);

        assertThat(updated.getFirstName()).isEqualTo("Updated");
    }

    @Test
    void updatingMissingTrainerThrows() {
        assertThatThrownBy(() -> storage.updateTrainer(sample("missing")))
                .isInstanceOf(TrainerDoesNotExistException.class);
    }

    @Test
    void gettingMissingTrainerReturnsEmpty(){
        assertThat(storage.getTrainer("missing")).isEmpty();
    }

    @Test
    void getAllTrainers() {
        storage.createTrainer(sample("a"));
        storage.createTrainer(sample("b"));

        assertThat(storage.getAllTrainers()).hasSize(2);
    }
}
