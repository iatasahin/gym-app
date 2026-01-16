package dev.ilkersahin.java.spring.gym.service;


import dev.ilkersahin.java.spring.gym.dao.TrainerDAO;
import dev.ilkersahin.java.spring.gym.exception.TrainerDoesNotExistException;
import dev.ilkersahin.java.spring.gym.exception.UsernameExistsException;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.service.util.PasswordGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.UserCreationService;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TrainerServiceTest {

    private TrainerService service;
    private TrainerDAO trainerDAO;
    private PasswordGeneratorService passwordService;
    private UserCreationService userCreationService;

    @BeforeEach
    void setUp() {
        trainerDAO = mock(TrainerDAO.class);
        passwordService = mock(PasswordGeneratorService.class);

        userCreationService = new UserCreationService(passwordService);
        userCreationService.setMaxSuffixRetriesForUsername(1000); // default value in application.properties

        service = new TrainerService();
        service.setTrainerDAO(trainerDAO);
        service.setUserCreationService(userCreationService);
    }

    private Trainer sample() {
        Trainer t = new Trainer();
        t.setFirstName("Tom");
        t.setLastName("Smith");
        t.setSpecialization(TrainingType.FITNESS);
        t.setUserId(UUID.randomUUID());
        return t;
    }

    private Trainer sample(String firstName, String lastName) {
        Trainer t = new Trainer();
        t.setFirstName(firstName);
        t.setLastName(lastName);
        t.setSpecialization(TrainingType.FITNESS);
        t.setUserId(UUID.randomUUID());
        t.setActive(true);
        return t;
    }

    // === CREATE TRAINER TESTS ===

    @Test
    @Order(101)
    void createTrainer_withValidTrainer_shouldGeneratePasswordAndDefaultUsername() {
        Trainer trainer = sample();

        when(passwordService.generate(10)).thenReturn("secretPass");
        when(trainerDAO.createTrainer(any(Trainer.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Trainer result = service.createTrainer(trainer);

        assertThat(result.getPassword()).isEqualTo("secretPass");
        assertThat(result.getUsername()).isEqualTo("Tom.Smith");
        verify(passwordService).generate(10);
        verify(trainerDAO).createTrainer(trainer);
    }

    @Test
    @Order(102)
    void createTrainer_withDuplicateUsername_shouldGenerateUniqueUsername() {
        Trainer trainer = sample();

        when(passwordService.generate(10)).thenReturn("secretPass");

        when(trainerDAO.createTrainer(any(Trainer.class)))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenAnswer(inv -> inv.getArgument(0));

        Trainer result = service.createTrainer(trainer);

        assertThat(result.getUsername()).isEqualTo("Tom.Smith2");
        assertThat(result.getPassword()).isEqualTo("secretPass");
        verify(trainerDAO, times(2)).createTrainer(any(Trainer.class));
    }

    @Test
    @Order(103)
    void createTrainer_withMultipleDuplicateUsernames_shouldIncrementUsernameSerially() {
        Trainer trainer = sample();

        when(passwordService.generate(10)).thenReturn("secretPass");
        when(trainerDAO.createTrainer(any(Trainer.class)))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenThrow(new UsernameExistsException("Username already exists"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Trainer result = service.createTrainer(trainer);

        assertThat(result.getUsername()).isEqualTo("Tom.Smith4");
        verify(trainerDAO, times(4)).createTrainer(any(Trainer.class));
    }

    @Test
    @Order(104)
    void createTrainer_withSpecialCharactersInName_shouldGenerateValidUsername() {
        Trainer trainer = sample("Tom-John", "O'Smith");

        when(passwordService.generate(10)).thenReturn("secretPass");
        when(trainerDAO.createTrainer(any(Trainer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Trainer result = service.createTrainer(trainer);

        assertThat(result.getUsername()).isEqualTo("Tom-John.O'Smith");
        assertThat(result.getPassword()).isEqualTo("secretPass");
    }

    // === GET TRAINER TESTS ===

    @Test
    @Order(201)
    void getTrainer_withExistingUsername_shouldReturnTrainer() {
        Trainer trainer = sample();
        trainer.setUsername("Tom.Smith");

        when(trainerDAO.getTrainer("Tom.Smith"))
                .thenReturn(Optional.of(trainer));

        Optional<Trainer> result = service.getTrainer("Tom.Smith");

        assertThat(result).contains(trainer);
        verify(trainerDAO).getTrainer("Tom.Smith");
    }

    @Test
    @Order(202)
    void getTrainer_withNonExistentUsername_shouldReturnEmpty() {
        when(trainerDAO.getTrainer("non.existent"))
                .thenReturn(Optional.empty());

        Optional<Trainer> result = service.getTrainer("non.existent");

        assertThat(result).isEmpty();
        verify(trainerDAO).getTrainer("non.existent");
    }

    @Test
    @Order(203)
    void getTrainer_withNullUsername_shouldDelegateToDAO() {
        when(trainerDAO.getTrainer(null))
                .thenReturn(Optional.empty());

        Optional<Trainer> result = service.getTrainer(null);

        assertThat(result).isEmpty();
        verify(trainerDAO).getTrainer(null);
    }

    @Test
    @Order(204)
    void getTrainer_withEmptyUsername_shouldDelegateToDAO() {
        when(trainerDAO.getTrainer(""))
                .thenReturn(Optional.empty());

        Optional<Trainer> result = service.getTrainer("");

        assertThat(result).isEmpty();
        verify(trainerDAO).getTrainer("");
    }

    // === UPDATE TRAINER TESTS ===

    @Test
    @Order(301)
    void updateTrainer_withValidTrainer_shouldDelegateToDAO() {
        Trainer trainer = sample();
        trainer.setUsername("Tom.Smith");

        when(trainerDAO.updateTrainer(trainer)).thenReturn(trainer);

        Trainer result = service.updateTrainer(trainer);

        assertThat(result).isSameAs(trainer);
        verify(trainerDAO).updateTrainer(trainer);
    }

    @Test
    @Order(302)
    void updateTrainer_withNonExistentTrainer_shouldThrowException() {
        Trainer trainer = sample();
        trainer.setUsername("non.existent");

        when(trainerDAO.updateTrainer(trainer))
                .thenThrow(new TrainerDoesNotExistException("Trainer does not exist"));

        assertThatThrownBy(() -> service.updateTrainer(trainer))
                .isInstanceOf(TrainerDoesNotExistException.class)
                .hasMessageContaining("Trainer does not exist");

        verify(trainerDAO).updateTrainer(trainer);
    }

    @Test
    @Order(303)
    void updateTrainer_shouldPreserveTrainerReference() {
        Trainer originalTrainer = sample();
        originalTrainer.setUsername("Tom.Smith");
        originalTrainer.setFirstName("UpdatedTom");

        when(trainerDAO.updateTrainer(originalTrainer)).thenReturn(originalTrainer);

        Trainer result = service.updateTrainer(originalTrainer);

        assertThat(result).isSameAs(originalTrainer);
        assertThat(result.getFirstName()).isEqualTo("UpdatedTom");
    }

    @Test
    @Order(304)
    void updateTrainer_withUpdatedSpecialization_shouldDelegateToDAO() {
        Trainer trainer = sample();
        trainer.setUsername("Tom.Smith");
        trainer.setSpecialization(TrainingType.YOGA);

        when(trainerDAO.updateTrainer(trainer)).thenReturn(trainer);

        Trainer result = service.updateTrainer(trainer);

        assertThat(result.getSpecialization()).isEqualTo(TrainingType.YOGA);
        verify(trainerDAO).updateTrainer(trainer);
    }

    // === GET ALL TRAINERS TESTS (501+) ===

    @Test
    @Order(501)
    void getAllTrainers_withExistingTrainers_shouldReturnAllTrainers() {
        Trainer trainer1 = sample("John", "Doe");
        Trainer trainer2 = sample("Jane", "Smith");

        when(trainerDAO.getAllTrainers())
                .thenReturn(List.of(trainer1, trainer2));

        List<Trainer> result = service.getAllTrainers();

        assertThat(result)
                .hasSize(2)
                .containsExactly(trainer1, trainer2);
        verify(trainerDAO).getAllTrainers();
    }

    @Test
    @Order(502)
    void getAllTrainers_withEmptyRepository_shouldReturnEmptyList() {
        when(trainerDAO.getAllTrainers()).thenReturn(List.of());

        List<Trainer> result = service.getAllTrainers();

        assertThat(result).isEmpty();
        verify(trainerDAO).getAllTrainers();
    }

    @Test
    @Order(503)
    void getAllTrainers_shouldPreserveTrainerOrder() {
        Trainer trainer1 = sample("Alpha", "Trainer");
        Trainer trainer2 = sample("Beta", "Trainer");
        Trainer trainer3 = sample("Gamma", "Trainer");

        when(trainerDAO.getAllTrainers())
                .thenReturn(List.of(trainer1, trainer2, trainer3));

        List<Trainer> result = service.getAllTrainers();

        assertThat(result).containsExactly(trainer1, trainer2, trainer3);
    }

    @Test
    @Order(504)
    void getAllTrainers_withDifferentSpecializations_shouldReturnAllTrainers() {
        Trainer fitnessTrainer = sample("Fitness", "Trainer");
        fitnessTrainer.setSpecialization(TrainingType.FITNESS);

        Trainer zumbaTrainer = sample("Zumba", "Trainer");
        zumbaTrainer.setSpecialization(TrainingType.ZUMBA);

        when(trainerDAO.getAllTrainers())
                .thenReturn(List.of(fitnessTrainer, zumbaTrainer));

        List<Trainer> result = service.getAllTrainers();

        assertThat(result)
                .hasSize(2)
                .extracting(Trainer::getSpecialization)
                .containsExactly(TrainingType.FITNESS, TrainingType.ZUMBA);
    }

    // === INTEGRATION TESTS ===

    @Test
    @Order(601)
    void createAndRetrieveTrainer_shouldMaintainDataConsistency() {
        Trainer trainer = sample();

        // Setup create operation
        when(passwordService.generate(10)).thenReturn("secretPass");
        when(trainerDAO.createTrainer(any(Trainer.class)))
                .thenAnswer(invocation -> {
                    Trainer t = invocation.getArgument(0);
                    t.setUsername("Tom.Smith");
                    return t;
                });

        // Setup get operation
        when(trainerDAO.getTrainer("Tom.Smith"))
                .thenReturn(Optional.of(trainer));

        // Execute operations
        Trainer created = service.createTrainer(trainer);
        Optional<Trainer> retrieved = service.getTrainer("Tom.Smith");

        // Verify consistency
        assertThat(created.getUsername()).isEqualTo("Tom.Smith");
        assertThat(created.getPassword()).isEqualTo("secretPass");
        assertThat(retrieved).isPresent().contains(trainer);
    }

    @Test
    @Order(602)
    void createUpdateAndRetrieve_shouldMaintainDataConsistency() {
        Trainer trainer = sample();

        // Setup create
        when(passwordService.generate(10)).thenReturn("secretPass");
        when(trainerDAO.createTrainer(any(Trainer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Setup update
        when(trainerDAO.updateTrainer(any(Trainer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Execute lifecycle
        Trainer created = service.createTrainer(trainer);
        created.setFirstName("UpdatedTom");
        created.setSpecialization(TrainingType.RESISTANCE);
        Trainer updated = service.updateTrainer(created);

        // Verify lifecycle
        assertThat(created.getUsername()).isEqualTo("Tom.Smith");
        assertThat(updated.getFirstName()).isEqualTo("UpdatedTom");
        assertThat(updated.getSpecialization()).isEqualTo(TrainingType.RESISTANCE);
    }

    @Test
    @Order(603)
    void dependencyInjection_shouldWorkCorrectly() {
        TrainerService newService = new TrainerService();
        TrainerDAO mockDAO = mock(TrainerDAO.class);
        UserCreationService mockUserCreationService = mock(UserCreationService.class);

        newService.setTrainerDAO(mockDAO);
        newService.setUserCreationService(mockUserCreationService);

        // Verify DAO injection
        when(mockDAO.getAllTrainers()).thenReturn(List.of());
        List<Trainer> result = newService.getAllTrainers();
        assertThat(result).isEmpty();
        verify(mockDAO).getAllTrainers();

        // Verify UserCreationService injection
        Trainer trainer = sample();
        when(mockUserCreationService.createUser(any(), any(), any())).thenReturn(trainer);
        newService.createTrainer(trainer);
        verify(mockUserCreationService).createUser(eq(trainer), any(), eq("Trainer"));
    }
}
