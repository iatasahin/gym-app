package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TraineeDAO;
import dev.ilkersahin.java.spring.gym.exception.TraineeDoesNotExistException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.service.util.PasswordGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.UsernameGeneratorService;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TraineeServiceTest {
    private TraineeService service;
    private TraineeDAO traineeDAO;
    private PasswordGeneratorService passwordGeneratorService;
    private UsernameGeneratorService usernameGeneratorService;

    @BeforeEach
    void setUp() {
        traineeDAO = mock(TraineeDAO.class);
        passwordGeneratorService = mock(PasswordGeneratorService.class);
        usernameGeneratorService = mock(UsernameGeneratorService.class);

        service = new TraineeService();
        service.setTraineeDAO(traineeDAO);
        service.setPasswordGeneratorService(passwordGeneratorService);
        service.setUsernameGeneratorService(usernameGeneratorService);
    }

    private Trainee sample() {
        return sample("Jack", "Black");
    }

    private Trainee sample(String firstName, String lastName) {
        return new Trainee(
                firstName, lastName, null, " ", true,
                LocalDate.of(1990, 1, 1), "123 Main Street", UUID.randomUUID()
        );
    }

    // === CREATE TRAINEE TESTS ===

    @Test
    @Order(101)
    void createTrainee_withValidTrainee_shouldGeneratePasswordAndDefaultUsername() {
        Trainee trainee = sample();

        when(usernameGeneratorService.generateUniqueUsername("Jack", "Black")).thenReturn("Jack.Black");
        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");
        when(traineeDAO.createTrainee(any(Trainee.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Trainee result = service.createTrainee(trainee);

        assertThat(result.getPassword()).isEqualTo("secretPass");
        assertThat(result.getUsername()).isEqualTo("Jack.Black");
        verify(passwordGeneratorService).generate(10);
        verify(traineeDAO).createTrainee(trainee);
    }

    @Test
    @Order(102)
    void createTrainee_withDuplicateUsername_shouldGenerateUniqueUsername() {
        Trainee trainee = sample();

        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");
        when(usernameGeneratorService.generateUniqueUsername("Jack", "Black")).thenReturn("Jack.Black2");
        when(traineeDAO.createTrainee(any(Trainee.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Trainee result = service.createTrainee(trainee);

        assertThat(result.getUsername()).isEqualTo("Jack.Black2");
        assertThat(result.getPassword()).isEqualTo("secretPass");
        verify(traineeDAO, times(1)).createTrainee(any(Trainee.class));
    }

    @Test
    @Order(104)
    void createTrainee_withSpecialCharactersInName_shouldGenerateValidUsername() {
        Trainee trainee = sample("Jack-John", "O'Black");
        when(usernameGeneratorService.generateUniqueUsername("Jack-John", "O'Black")).thenReturn("Jack-John.O'Black");
        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");
        when(traineeDAO.createTrainee(any(Trainee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Trainee result = service.createTrainee(trainee);

        assertThat(result.getUsername()).isEqualTo("Jack-John.O'Black");
        assertThat(result.getPassword()).isEqualTo("secretPass");
    }

    // === GET TRAINEE TESTS ===

    @Test
    @Order(201)
    void getTrainee_withExistingUsername_shouldReturnTrainee() {
        Trainee trainee = sample();
        trainee.setUsername("Jack.Black");

        when(traineeDAO.getTrainee("Jack.Black"))
                .thenReturn(Optional.of(trainee));

        Optional<Trainee> result = service.getTrainee("Jack.Black");

        assertThat(result)
                .isPresent()
                .contains(trainee);
        verify(traineeDAO).getTrainee("Jack.Black");
    }

    @Test
    @Order(202)
    void getTrainee_withNonExistentUsername_shouldReturnEmpty() {
        when(traineeDAO.getTrainee("non.existent"))
                .thenReturn(Optional.empty());

        Optional<Trainee> result = service.getTrainee("non.existent");

        assertThat(result).isEmpty();
        verify(traineeDAO).getTrainee("non.existent");
    }

    @Test
    @Order(203)
    void getTrainee_withNullUsername_shouldDelegateToDAO() {
        when(traineeDAO.getTrainee(null)).thenReturn(Optional.empty());

        Optional<Trainee> result = service.getTrainee(null);

        assertThat(result).isEmpty();
        verify(traineeDAO).getTrainee(null);
    }

    @Test
    @Order(204)
    void getTrainee_withEmptyUsername_shouldDelegateToDAO() {
        when(traineeDAO.getTrainee("")).thenReturn(Optional.empty());

        Optional<Trainee> result = service.getTrainee("");

        assertThat(result).isEmpty();
        verify(traineeDAO).getTrainee("");
    }

    // === UPDATE TRAINEE TESTS ===

    @Test
    @Order(301)
    void updateTrainee_withValidTrainee_shouldDelegateToDAO() {
        Trainee trainee = sample();
        trainee.setUsername("Jack.Black");

        when(traineeDAO.updateTrainee(trainee)).thenReturn(trainee);

        Trainee result = service.updateTrainee(trainee);

        assertThat(result).isSameAs(trainee);
        verify(traineeDAO).updateTrainee(trainee);
    }

    @Test
    @Order(302)
    void updateTrainee_withNonExistentTrainee_shouldThrowException() {
        Trainee trainee = sample();
        trainee.setUsername("non.existent");

        when(traineeDAO.updateTrainee(trainee))
                .thenThrow(new TraineeDoesNotExistException("Trainee does not exist"));

        assertThatThrownBy(() -> service.updateTrainee(trainee))
                .isInstanceOf(TraineeDoesNotExistException.class)
                .hasMessageContaining("Trainee does not exist");

        verify(traineeDAO).updateTrainee(trainee);
    }

    @Test
    @Order(303)
    void updateTrainee_shouldPreserveTraineeReference() {
        Trainee originalTrainee = sample();
        originalTrainee.setUsername("Jack.Black");
        originalTrainee.setFirstName("UpdatedJack");

        when(traineeDAO.updateTrainee(originalTrainee)).thenReturn(originalTrainee);

        Trainee result = service.updateTrainee(originalTrainee);

        assertThat(result).isSameAs(originalTrainee);
        assertThat(result.getFirstName()).isEqualTo("UpdatedJack");
    }

    // === DELETE TRAINEE TESTS ===

    @Test
    @Order(401)
    void deleteTrainee_withExistingUsername_shouldReturnDeletedTrainee() {
        Trainee trainee = sample();
        trainee.setUsername("Jack.Black");

        when(traineeDAO.deleteTrainee("Jack.Black"))
                .thenReturn(Optional.of(trainee));

        Optional<Trainee> result = service.deleteTrainee("Jack.Black");

        assertThat(result)
                .isPresent()
                .contains(trainee);
        verify(traineeDAO).deleteTrainee("Jack.Black");
    }

    @Test
    @Order(402)
    void deleteTrainee_withNonExistentUsername_shouldReturnEmpty() {
        when(traineeDAO.deleteTrainee("non.existent"))
                .thenReturn(Optional.empty());

        Optional<Trainee> result = service.deleteTrainee("non.existent");

        assertThat(result).isEmpty();
        verify(traineeDAO).deleteTrainee("non.existent");
    }

    @Test
    @Order(403)
    void deleteTrainee_withNullUsername_shouldDelegateToDAO() {
        when(traineeDAO.deleteTrainee(null)).thenReturn(Optional.empty());

        Optional<Trainee> result = service.deleteTrainee(null);

        assertThat(result).isEmpty();
        verify(traineeDAO).deleteTrainee(null);
    }

    @Test
    @Order(404)
    void deleteTrainee_withEmptyUsername_shouldDelegateToDAO() {
        when(traineeDAO.deleteTrainee("")).thenReturn(Optional.empty());

        Optional<Trainee> result = service.deleteTrainee("");

        assertThat(result).isEmpty();
        verify(traineeDAO).deleteTrainee("");
    }

    // === GET ALL TRAINEES TESTS ===

    @Test
    @Order(501)
    void getAllTrainees_withExistingTrainees_shouldReturnAllTrainees() {
        Trainee trainee1 = sample("John", "Doe");
        Trainee trainee2 = sample("Jane", "Smith");

        when(traineeDAO.getAllTrainees())
                .thenReturn(List.of(trainee1, trainee2));

        List<Trainee> result = service.getAllTrainees();

        assertThat(result)
                .hasSize(2)
                .containsExactly(trainee1, trainee2);

        verify(traineeDAO).getAllTrainees();
    }


    @Test
    @Order(502)
    void getAllTrainees_withEmptyRepository_shouldReturnEmptyList() {
        when(traineeDAO.getAllTrainees()).thenReturn(List.of());

        List<Trainee> result = service.getAllTrainees();

        assertThat(result).isEmpty();
        verify(traineeDAO).getAllTrainees();
    }

    @Test
    @Order(503)
    void getAllTrainees_shouldPreserveTraineeOrder() {
        Trainee trainee1 = sample("Alpha", "Trainee");
        Trainee trainee2 = sample("Beta", "Trainee");
        Trainee trainee3 = sample("Gamma", "Trainee");

        when(traineeDAO.getAllTrainees()).thenReturn(List.of(trainee1, trainee2, trainee3));

        List<Trainee> result = service.getAllTrainees();

        assertThat(result).containsExactly(trainee1, trainee2, trainee3);
    }

    // === INTEGRATION TESTS ===

    @Test
    @Order(601)
    void createAndRetrieveTrainee_shouldMaintainDataConsistency() {
        Trainee trainee = sample();

        // Setup create operation
        when(passwordGeneratorService.generate(10)).thenReturn("secretPass");
        when(traineeDAO.createTrainee(any(Trainee.class)))
                .thenAnswer(invocation -> {
                    Trainee t = invocation.getArgument(0);
                    t.setUsername("Jack.Black");
                    return t;
                });

        // Setup get operation
        when(traineeDAO.getTrainee("Jack.Black")).thenReturn(Optional.of(trainee));

        // Execute operations
        Trainee created = service.createTrainee(trainee);
        Optional<Trainee> retrieved = service.getTrainee("Jack.Black");

        // Verify consistency
        assertThat(created.getUsername()).isEqualTo("Jack.Black");
        assertThat(created.getPassword()).isEqualTo("secretPass");
        assertThat(retrieved).isPresent().contains(trainee);
    }

    @Test
    @Order(602)
    void createUpdateDeleteAndRetrieve_shouldMaintainDataConsistency() {
        Trainee trainee = sample();

        // Setup create
        when(passwordGeneratorService.generate(10)).thenReturn("password123");
        when(usernameGeneratorService.generateUniqueUsername("Jack", "Black")).thenReturn("Jack.Black");
        when(traineeDAO.createTrainee(any(Trainee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Setup update
        when(traineeDAO.updateTrainee(any(Trainee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Setup delete
        when(traineeDAO.deleteTrainee("Jack.Black")).thenReturn(Optional.of(trainee));

        // Execute full lifecycle
        Trainee created = service.createTrainee(trainee);
        created.setFirstName("UpdatedJack");
        Trainee updated = service.updateTrainee(created);
        Optional<Trainee> deleted = service.deleteTrainee("Jack.Black");

        // Verify lifecycle
        assertThat(created.getUsername()).isEqualTo("Jack.Black");
        assertThat(updated.getFirstName()).isEqualTo("UpdatedJack");
        assertThat(deleted).isPresent().contains(trainee);
    }

    @Test
    @Order(603)
    void dependencyInjection_shouldWorkCorrectly() {
        TraineeService newService = new TraineeService();
        TraineeDAO mockDAO = mock(TraineeDAO.class);
        PasswordGeneratorService mockPasswordGeneratorService = mock(PasswordGeneratorService.class);
        UsernameGeneratorService mockUsernameGeneratorService = mock(UsernameGeneratorService.class);

        newService.setTraineeDAO(mockDAO);
        newService.setPasswordGeneratorService(mockPasswordGeneratorService);
        newService.setUsernameGeneratorService(mockUsernameGeneratorService);

        // Verify DAO injection
        when(mockDAO.getAllTrainees()).thenReturn(List.of());
        List<Trainee> result = newService.getAllTrainees();
        assertThat(result).isEmpty();
        verify(mockDAO).getAllTrainees();

        // Verify UsernameGeneratorService and PasswordGeneratorService injection
        Trainee trainee = sample();
        when(mockUsernameGeneratorService.generateUniqueUsername("Jack", "Black")).thenReturn("Jack.Black3");
        when(mockPasswordGeneratorService.generate(10)).thenReturn("secretPass");
        when(mockDAO.createTrainee(any(Trainee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Trainee saved = newService.createTrainee(trainee);

        assertThat(saved.getPassword()).isEqualTo("secretPass");
        assertThat(saved.getUsername()).isEqualTo("Jack.Black3");
        verify(mockUsernameGeneratorService).generateUniqueUsername("Jack", "Black");
        verify(mockPasswordGeneratorService).generate(10);
    }
}
