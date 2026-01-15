package dev.ilkersahin.java.spring.gym.storage;

import dev.ilkersahin.java.spring.gym.exception.TrainerDoesNotExistException;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
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

    // === CREATE TRAINER TESTS ===

    @Test
    void createTrainer_withValidTrainer_shouldStoreAndReturnTrainer() {
        Trainer trainer = sample("tom.smith");

        Trainer result = storage.createTrainer(trainer);

        assertThat(result).isEqualTo(trainer);
        assertThat(storage.getTrainer("tom.smith"))
                .isPresent()
                .contains(trainer);
    }

    @Test
    void createTrainer_withDuplicateUsername_shouldThrowUsernameExistsException() {
        storage.createTrainer(sample("tom.smith"));

        assertThatThrownBy(() -> storage.createTrainer(sample("tom.smith")))
                .isInstanceOf(UsernameExistsException.class);
    }

    @Test
    void createTrainer_withNullUsername_shouldThrowException() {
        Trainer trainer = sample(null);

        assertThatThrownBy(() -> storage.createTrainer(trainer))
                .isInstanceOf(NullPointerException.class);
    }

    // === GET TRAINER TESTS ===

    @Test
    void getTrainer_withExistingUsername_shouldReturnTrainer() {
        Trainer trainer = sample("tom.smith");
        storage.createTrainer(trainer);

        Optional<Trainer> result = storage.getTrainer("tom.smith");

        assertThat(result)
                .isPresent()
                .contains(trainer);
    }

    @Test
    void getTrainer_withNonExistentUsername_shouldReturnEmpty(){
        assertThat(storage.getTrainer("non.existent")).isEmpty();
    }

    // === UPDATE TRAINER TESTS ===

    @Test
    void updateTrainer_withExistingTrainer_shouldUpdateAndReturnModifiedTrainer() {
        Trainer originalTrainer = sample("tom.smith");
        storage.createTrainer(originalTrainer);

        originalTrainer.setFirstName("UpdatedTom");
        originalTrainer.setLastName("UpdatedSmith");
        originalTrainer.setActive(false);

        Trainer result = storage.updateTrainer(originalTrainer);

        assertThat(result.getFirstName()).isEqualTo("UpdatedTom");
        assertThat(result.getLastName()).isEqualTo("UpdatedSmith");
        assertThat(result.isActive()).isFalse();
        assertThat(result.getUsername()).isEqualTo("tom.smith"); // Username should remain unchanged
        assertThat(result.getSpecialization()).isEqualTo(TrainingType.FITNESS); // Specialization should remain unchanged
    }

    @Test
    void updateTrainer_withNonExistentTrainer_shouldThrowTrainerDoesNotExistException() {
        assertThatThrownBy(() -> storage.updateTrainer(sample("non.existent")))
                .isInstanceOf(TrainerDoesNotExistException.class)
                .hasMessageContaining( "Trainer with Username 'non.existent' does not exist");
    }

    // === GET ALL TRAINERS TESTS ===

    @Test
    void getAllTrainers_withEmptyStorage_shouldReturnEmptyList() {
        List<Trainer> result = storage.getAllTrainers();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllTrainers_withMultipleTrainers_shouldReturnAllTrainers() {
        Trainer trainer1 = sample("trainer1");
        Trainer trainer2 = sample("trainer2");
        Trainer trainer3 = sample("trainer3");

        storage.createTrainer(trainer1);
        storage.createTrainer(trainer2);
        storage.createTrainer(trainer3);

        List<Trainer> result = storage.getAllTrainers();

        assertThat(result)
                .hasSize(3)
                .containsExactlyInAnyOrder(trainer1, trainer2, trainer3);
    }

    @Test
    void getAllTrainers_afterUpdatingTrainer_shouldReturnUpdatedTrainer() {
        Trainer trainer = sample("tom.smith");
        storage.createTrainer(trainer);

        trainer.setFirstName("Updated");
        storage.updateTrainer(trainer);

        List<Trainer> result = storage.getAllTrainers();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getFirstName()).isEqualTo("Updated");
    }

    // === INTEGRATION TESTS ===

    @Test
    void createUpdateAndRetrieve_shouldMaintainDataConsistency() {
        // Create
        Trainer trainer = sample("integration.test");
        storage.createTrainer(trainer);

        // Update
        trainer.setFirstName("IntegrationUpdated");
        trainer.setActive(false);
        storage.updateTrainer(trainer);

        // Retrieve and verify
        Optional<Trainer> retrieved = storage.getTrainer("integration.test");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getFirstName()).isEqualTo("IntegrationUpdated");
        assertThat(retrieved.get().isActive()).isFalse();

        // Verify in getAllTrainers
        List<Trainer> allTrainers = storage.getAllTrainers();
        assertThat(allTrainers).hasSize(1);
        assertThat(allTrainers.getFirst().getFirstName()).isEqualTo("IntegrationUpdated");
    }
}
