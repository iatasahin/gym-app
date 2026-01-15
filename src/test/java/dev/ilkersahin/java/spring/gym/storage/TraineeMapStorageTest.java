package dev.ilkersahin.java.spring.gym.storage;

import dev.ilkersahin.java.spring.gym.exception.TraineeDoesNotExistException;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    // === CREATE TRAINEE TESTS ===

    @Test
    void createTrainee_withValidTrainee_shouldStoreAndReturnTrainee() {
        Trainee trainee = sample("jack.black");

        Trainee result = storage.createTrainee(trainee);

        assertThat(result).isEqualTo(trainee);
        assertThat(storage.getTrainee("jack.black"))
                .isPresent()
                .contains(trainee);
    }

    @Test
    void createTrainee_withDuplicateUsername_shouldThrowUsernameExistsException() {
        storage.createTrainee(sample("jack.black"));

        assertThatThrownBy(
                () -> storage.createTrainee(sample("jack.black"))
        ).isInstanceOf(UsernameExistsException.class);
    }

    @Test
    void createTrainee_withNullUsername_shouldThrowException() {
        Trainee trainee = sample(null);

        assertThatThrownBy(() -> storage.createTrainee(trainee))
                .isInstanceOf(NullPointerException.class);
    }

    // === GET TRAINEE TESTS ===

    @Test
    void getTrainee_withExistingUsername_shouldReturnTrainee() {
        Trainee trainee = sample("jack.black");
        storage.createTrainee(trainee);

        Optional<Trainee> result = storage.getTrainee("jack.black");

        assertThat(result)
                .isPresent()
                .contains(trainee);
    }

    @Test
    void getTrainee_withNonExistentUsername_shouldReturnEmpty() {
        Optional<Trainee> result = storage.getTrainee("non.existent");

        assertThat(result).isEmpty();
    }

    // === UPDATE TRAINEE TESTS ===

    @Test
    void updateTrainee_withExistingTrainee_shouldUpdateAndReturnModifiedTrainee() {
        Trainee originalTrainee = sample("jack.black");
        storage.createTrainee(originalTrainee);

        originalTrainee.setFirstName("UpdatedJack");
        originalTrainee.setLastName("UpdatedBlack");
        originalTrainee.setDateOfBirth(LocalDate.of(1985, 5, 15));
        originalTrainee.setAddress("Updated Address");
        originalTrainee.setActive(false);

        Trainee result = storage.updateTrainee(originalTrainee);

        assertThat(result.getFirstName()).isEqualTo("UpdatedJack");
        assertThat(result.getLastName()).isEqualTo("UpdatedBlack");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 5, 15));
        assertThat(result.getAddress()).isEqualTo("Updated Address");
        assertThat(result.isActive()).isFalse();
        assertThat(result.getUsername()).isEqualTo("jack.black"); // Username should remain unchanged
    }

    @Test
    void updateTrainee_withNonExistentTrainee_shouldThrowTraineeDoesNotExistException() {
        Trainee nonExistentTrainee = sample("non.existent");

        assertThatThrownBy(() -> storage.updateTrainee(nonExistentTrainee))
                .isInstanceOf(TraineeDoesNotExistException.class)
                .hasMessageContaining("Trainee with Username 'non.existent' does not exist");
    }

    // === DELETE TRAINEE TESTS ===

    @Test
    void deleteTrainee_withExistingTrainee_shouldRemoveAndReturnTrainee() {
        Trainee trainee = sample("jack.black");
        storage.createTrainee(trainee);
        Optional<Trainee> result = storage.deleteTrainee("jack.black");

        assertThat(result)
                .isPresent()
                .contains(trainee);
        assertThat(storage.getTrainee("jack.black")).isEmpty();
    }

    @Test
    void deleteTrainee_withNonExistentTrainee_shouldReturnEmptyWithoutException() {
        Optional<Trainee> result = storage.deleteTrainee("non.existent");

        assertThat(result).isEmpty();
        assertThat(storage.getTrainee("non.existent")).isEmpty();
    }

    @Test
    void deleteTrainee_afterDeletion_shouldNotAffectOtherTrainees() {
        Trainee trainee1 = sample("trainee1");
        Trainee trainee2 = sample("trainee2");
        storage.createTrainee(trainee1);
        storage.createTrainee(trainee2);

        storage.deleteTrainee("trainee1");

        assertThat(storage.getTrainee("trainee1")).isEmpty();
        assertThat(storage.getTrainee("trainee2"))
                .isPresent()
                .contains(trainee2);
        assertThat(storage.getAllTrainees()).hasSize(1);
    }

    // === GET ALL TRAINEES TESTS ===

    @Test
    void getAllTrainees_withEmptyStorage_shouldReturnEmptyList() {
        List<Trainee> result = storage.getAllTrainees();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllTrainees_withMultipleTrainees_shouldReturnAllTrainees() {
        Trainee trainee1 = sample("trainee1");
        Trainee trainee2 = sample("trainee2");
        Trainee trainee3 = sample("trainee3");

        storage.createTrainee(trainee1);
        storage.createTrainee(trainee2);
        storage.createTrainee(trainee3);

        List<Trainee> result = storage.getAllTrainees();

        assertThat(result)
                .hasSize(3)
                .containsExactlyInAnyOrder(trainee1, trainee2, trainee3);
    }


    @Test
    void getAllTrainees_afterUpdatingTrainee_shouldReturnUpdatedTrainee() {
        Trainee trainee = sample("jack.black");
        storage.createTrainee(trainee);

        trainee.setFirstName("Updated");
        storage.updateTrainee(trainee);

        List<Trainee> result = storage.getAllTrainees();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Updated");
    }


    @Test
    void getAllTrainees_afterDeletingTrainee_shouldNotIncludeDeletedTrainee() {
        Trainee trainee1 = sample("trainee1");
        Trainee trainee2 = sample("trainee2");
        storage.createTrainee(trainee1);
        storage.createTrainee(trainee2);

        storage.deleteTrainee("trainee1");

        List<Trainee> result = storage.getAllTrainees();
        assertThat(result)
                .hasSize(1)
                .containsExactly(trainee2);
    }

    // === INTEGRATION TESTS ===

    @Test
    void createUpdateDeleteAndRetrieve_shouldMaintainDataConsistency() {
        // Create
        Trainee trainee = sample("integration.test");
        storage.createTrainee(trainee);

        // Update
        trainee.setFirstName("IntegrationUpdated");
        trainee.setAddress("Updated Address");
        trainee.setActive(false);
        storage.updateTrainee(trainee);

        // Verify update
        Optional<Trainee> retrieved = storage.getTrainee("integration.test");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getFirstName()).isEqualTo("IntegrationUpdated");
        assertThat(retrieved.get().getAddress()).isEqualTo("Updated Address");
        assertThat(retrieved.get().isActive()).isFalse();

        // Delete
        Optional<Trainee> deleted = storage.deleteTrainee("integration.test");
        assertThat(deleted).isPresent();
        assertThat(deleted.get().getFirstName()).isEqualTo("IntegrationUpdated");

        // Verify deletion
        assertThat(storage.getTrainee("integration.test")).isEmpty();
        assertThat(storage.getAllTrainees()).isEmpty();
    }
}
