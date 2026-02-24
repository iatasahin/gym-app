package dev.ilkersahin.java.spring.gym.service.impl;

import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import dev.ilkersahin.java.spring.gym.dto.request.ActivationRequest;
import dev.ilkersahin.java.spring.gym.dto.request.PasswordChangeRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeDeleteRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeTrainerListUpdateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeUpdateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TrainingSearchRequestForTrainee;
import dev.ilkersahin.java.spring.gym.dto.response.ActivationResponse;
import dev.ilkersahin.java.spring.gym.dto.response.UserCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.view.TraineeView;
import dev.ilkersahin.java.spring.gym.dto.view.TraineeWithListView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerInfo;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.exception.InvalidCredentialsException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.model.User;
import dev.ilkersahin.java.spring.gym.repository.TraineeRepository;
import dev.ilkersahin.java.spring.gym.repository.TrainerRepository;
import dev.ilkersahin.java.spring.gym.repository.TrainingRepository;
import dev.ilkersahin.java.spring.gym.repository.UserRepository;
import dev.ilkersahin.java.spring.gym.service.util.PasswordGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.UsernameGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.ViewMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TraineeServiceImplTest {

    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;
    @Mock private TrainingRepository trainingRepository;
    @Mock private UserRepository userRepository;
    @Mock private UsernameGeneratorService usernameGeneratorService;
    @Mock private PasswordGeneratorService passwordGeneratorService;
    @Mock private ViewMapper viewMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private User activeUser;
    private User inactiveUser;
    private Trainee activeTrainee;
    private Trainee inactiveTrainee;
    private Credentials validCredentials;
    private Credentials wrongPasswordCredentials;
    private Credentials nonExistentCredentials;
    private TraineeView traineeView;

    @BeforeEach
    void setUp() {
        activeUser = new User("Jack", "Black", "Jack.Black", "password123", true);
        inactiveUser = new User("Jane", "Doe", "Jane.Doe", "password456", false);

        activeTrainee = new Trainee();
        activeTrainee.setUser(activeUser);
        activeTrainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        activeTrainee.setAddress("123 Main St");

        inactiveTrainee = new Trainee();
        inactiveTrainee.setUser(inactiveUser);

        validCredentials = new Credentials("Jack.Black", "password123");
        wrongPasswordCredentials = new Credentials("Jack.Black", "wrongPassword");
        nonExistentCredentials = new Credentials("Non.Existent", "password");

        traineeView = new TraineeView(
                "Jack.Black", "Jack", "Black", true,
                LocalDate.of(1990, 1, 1), "123 Main St"
        );
    }

    // =========================================================================
    // CREATE TRAINEE TESTS (100s)
    // =========================================================================


    @Test
    @Order(101)
    void createTrainee_withValidRequest_shouldReturnResponseWithUsernameAndPassword() {
        TraineeCreateRequest request = new TraineeCreateRequest(
                "Jack", "Black", LocalDate.of(1990, 1, 1), "123 Main St"
        );

        when(usernameGeneratorService.generateUniqueUsername("Jack", "Black")).thenReturn("Jack.Black");
        when(passwordGeneratorService.generate(10)).thenReturn("password123");
        when(traineeRepository.save(any(Trainee.class))).thenReturn(activeTrainee);

        UserCreateResponse response = traineeService.createTrainee(request);

        assertThat(response.username()).isEqualTo("Jack.Black");
        assertThat(response.password()).isEqualTo("password123");
        verify(userRepository).save(any(User.class));
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    @Order(102)
    void createTrainee_withNullOptionalFields_shouldSucceed() {
        TraineeCreateRequest request = new TraineeCreateRequest(
                "Jack", "Black", null, null
        );

        when(usernameGeneratorService.generateUniqueUsername("Jack", "Black")).thenReturn("Jack.Black");
        when(passwordGeneratorService.generate(10)).thenReturn("password123");
        when(traineeRepository.save(any(Trainee.class))).thenReturn(activeTrainee);

        UserCreateResponse response = traineeService.createTrainee(request);

        assertThat(response).isNotNull();
        assertThat(response.username()).isNotNull();
    }

    // =========================================================================
    // GET TRAINEE TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void getTrainee_withValidCredentials_shouldReturnTrainee() {
        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(traineeRepository.findAssignedTrainers("Jack.Black")).thenReturn(List.of());

        TraineeWithListView response = traineeService.getTrainee(validCredentials.username());

        assertThat(response.username()).isEqualTo("Jack.Black");
        assertThat(response.firstName()).isEqualTo("Jack");
        assertThat(response.trainers()).isNotNull();
        assertThat(response.trainers()).isEmpty();
    }

    // =========================================================================
    // UPDATE TRAINEE TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void updateTrainee_withAllFields_shouldUpdateAllFields() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                validCredentials.username(), "John", "Doe", true,
                LocalDate.of(1985, 5, 5), "456 Oak St"
        );
        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(traineeRepository.save(activeTrainee)).thenReturn(activeTrainee);

        TraineeWithListView response = traineeService.updateTrainee(request);

        assertThat(activeTrainee.getUser().getFirstName()).isEqualTo("John");
        assertThat(activeTrainee.getUser().getLastName()).isEqualTo("Doe");
        assertThat(activeTrainee.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 5, 5));
        assertThat(activeTrainee.getAddress()).isEqualTo("456 Oak St");
    }

    @Test
    @Order(302)
    void updateTrainee_withOnlyFirstName_shouldUpdateOnlyFirstName() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                validCredentials.username(), "John", null, null, null, null
        );

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(traineeRepository.save(activeTrainee)).thenReturn(activeTrainee);

        traineeService.updateTrainee(request);

        assertThat(activeTrainee.getUser().getFirstName()).isEqualTo("John");
        assertThat(activeTrainee.getUser().getLastName()).isEqualTo("Black"); // unchanged
    }

    @Test
    @Order(303)
    void updateTrainee_withOnlyLastName_shouldUpdateOnlyLastName() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                validCredentials.username(), null, "Doe", null, null, null
        );

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(traineeRepository.save(activeTrainee)).thenReturn(activeTrainee);

        traineeService.updateTrainee(request);

        assertThat(activeTrainee.getUser().getFirstName()).isEqualTo("Jack"); // unchanged
        assertThat(activeTrainee.getUser().getLastName()).isEqualTo("Doe");
    }

    @Test
    @Order(304)
    void updateTrainee_withOnlyDateOfBirth_shouldUpdateOnlyDateOfBirth() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                validCredentials.username(), null, null, null,
                LocalDate.of(1995, 12, 25), null
        );

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(traineeRepository.save(activeTrainee)).thenReturn(activeTrainee);

        traineeService.updateTrainee(request);

        assertThat(activeTrainee.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 12, 25));
        assertThat(activeTrainee.getAddress()).isEqualTo("123 Main St"); // unchanged
    }

    @Test
    @Order(305)
    void updateTrainee_withOnlyAddress_shouldUpdateOnlyAddress() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                validCredentials.username(), null, null, null, null,
                "789 Pine Ave"
        );

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(traineeRepository.save(activeTrainee)).thenReturn(activeTrainee);

        traineeService.updateTrainee(request);

        assertThat(activeTrainee.getAddress()).isEqualTo("789 Pine Ave");
        assertThat(activeTrainee.getUser().getFirstName()).isEqualTo("Jack"); // unchanged
    }

    @Test
    @Order(306)
    void updateTrainee_withNoFields_shouldNotChangeAnything() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                validCredentials.username(), null, null, null, null, null
        );

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(traineeRepository.save(activeTrainee)).thenReturn(activeTrainee);

        TraineeWithListView response = traineeService.updateTrainee(request);

        assertThat(response.username()).isEqualTo(validCredentials.username());
        assertThat(activeTrainee.getUser().getFirstName()).isEqualTo("Jack");
        assertThat(activeTrainee.getUser().getLastName()).isEqualTo("Black");
    }

    // =========================================================================
    // CHANGE PASSWORD TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void changePassword_withValidCredentials_shouldUpdatePassword() {
        PasswordChangeRequest request = new PasswordChangeRequest(
                validCredentials.username(), validCredentials.password(),
                "newSecurePassword"
        );

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(passwordEncoder.matches(validCredentials.password(), activeTrainee.getUser().getPassword()))
                .thenReturn(true);
        when(passwordEncoder.encode("newSecurePassword"))
                .thenReturn("$2a$10$hashedNewPassword");

        Boolean response = traineeService.changePassword(request);

        assertThat(response).isTrue();
        assertThat(activeTrainee.getUser().getPassword()).isEqualTo("$2a$10$hashedNewPassword");
        verify(userRepository).save(activeTrainee.getUser());
        verify(passwordEncoder).encode("newSecurePassword");
    }

    @Test
    @Order(402)
    void changePassword_withInvalidOldPassword_shouldThrowException() {
        PasswordChangeRequest request = new PasswordChangeRequest(
                validCredentials.username(), "wrongOldPassword",
                "newSecurePassword"
        );

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(passwordEncoder.matches("wrongOldPassword", activeTrainee.getUser().getPassword()))
                .thenReturn(false);

        assertThatThrownBy(() -> traineeService.changePassword(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Old password is incorrect");

        verify(userRepository, never()).save(any());
    }

    // =========================================================================
    // ACTIVATE / DEACTIVATE TESTS (500s)
    // =========================================================================

    @Test
    @Order(501)
    void activate_withValidCredentials_shouldSetActiveTrue() {
        activeUser.setActive(false); // start inactive for this test
        ActivationRequest request = new ActivationRequest("Jack.Black", true);

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));

        ActivationResponse response = traineeService.activate(request);

        assertThat(response.active()).isTrue();
        assertThat(activeTrainee.getUser().isActive()).isTrue();
    }

    @Test
    @Order(502)
    void deactivate_withValidCredentials_shouldSetActiveFalse() {
        ActivationRequest request = new ActivationRequest(validCredentials.username(), false);

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));

        ActivationResponse response = traineeService.deactivate(request);

        assertThat(response.active()).isFalse();
        assertThat(activeTrainee.getUser().isActive()).isFalse();
    }

    // =========================================================================
    // DELETE TRAINEE TESTS (600s)
    // =========================================================================

    @Test
    @Order(601)
    void deleteTrainee_withValidCredentials_shouldDeleteAndReturnSuccess() {
        TraineeDeleteRequest request = new TraineeDeleteRequest(validCredentials);

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));

        Boolean response = traineeService.deleteTrainee(request.credentials().username());

        assertThat(response).isTrue();
        verify(traineeRepository).delete(activeTrainee);
    }

    // =========================================================================
    // GET UNASSIGNED TRAINERS TESTS (700s)
    // =========================================================================

    @Test
    @Order(701)
    void getUnassignedTrainers_withValidCredentials_shouldReturnTrainerList() {
        Trainer trainer = new Trainer();
        User trainerUser = new User("Tom", "Smith", "Tom.Smith", "pass", true);
        trainer.setUser(trainerUser);
        trainer.setSpecializationType(TrainingType.Type.YOGA);

        TrainerView trainerView = new TrainerView(
                "Tom.Smith", "Tom", "Smith", true, "Fitness"
        );

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(trainerRepository.findTrainersNotAssignedToTrainee("Jack.Black")).thenReturn(List.of(trainer));

        List<TrainerInfo> response = traineeService.getUnassignedTrainers(validCredentials.username());

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().username()).isEqualTo("Tom.Smith");
    }

    @Test
    @Order(702)
    void getUnassignedTrainers_withNoAvailableTrainers_shouldReturnEmptyList() {
        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(trainerRepository.findTrainersNotAssignedToTrainee("Jack.Black")).thenReturn(List.of());

        List<TrainerInfo> response = traineeService.getUnassignedTrainers(validCredentials.username());

        assertThat(response).isEmpty();
    }

    // =========================================================================
    // UPDATE TRAINERS TESTS (800s)
    // =========================================================================

    @Test
    @Order(801)
    void updateTrainers_withValidUsernames_shouldUpdateTrainerList() {
        TraineeTrainerListUpdateRequest request = new TraineeTrainerListUpdateRequest(
                validCredentials.username(), List.of("Tom.Smith", "Jane.Doe")
        );

        Trainer trainer1 = new Trainer();
        User trainerUser1 = new User("Tom", "Smith", "Tom.Smith", "pass", true);
        trainer1.setUser(trainerUser1);
        trainer1.setSpecializationType(TrainingType.Type.FITNESS);

        Trainer trainer2 = new Trainer();
        User trainerUser2 = new User("Jane", "Doe", "Jane.Doe", "pass", true);
        trainer2.setUser(trainerUser2);
        trainer2.setSpecializationType(TrainingType.Type.YOGA);

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(trainerRepository.findByUserUsernames(List.of("Tom.Smith", "Jane.Doe")))
                .thenReturn(List.of(trainer1, trainer2));

        List<TrainerInfo> response = traineeService.updateTrainers(request);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.getFirst().username()).isEqualTo("Tom.Smith");
        assertThat(response.get(1).username()).isEqualTo("Jane.Doe");
        verify(traineeRepository).save(activeTrainee);
    }

    @Test
    @Order(802)
    void updateTrainers_withEmptyList_shouldClearTrainers() {
        TraineeTrainerListUpdateRequest request = new TraineeTrainerListUpdateRequest(
                validCredentials.username(), List.of()
        );

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(trainerRepository.findByUserUsernames(List.of())).thenReturn(List.of());

        List<TrainerInfo>  response = traineeService.updateTrainers(request);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(0);
        verify(traineeRepository).save(activeTrainee);
    }

    // =========================================================================
    // GET TRAININGS TESTS (900s)
    // =========================================================================

    @Test
    @Order(901)
    void getTrainings_withNoFilters_shouldReturnAllTrainings() {
        TrainingSearchRequestForTrainee request = new TrainingSearchRequestForTrainee(
                validCredentials.username(), null, null, null, null
        );

        Training training = new Training();
        TrainingView trainingView = new TrainingView(
                "Morning Session", LocalDate.now(), 60,
                TrainingType.Type.FITNESS.getName(), "Jack.Black", "Tom.Smith"
        );

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(trainingRepository.findForTrainee("Jack.Black", null, null, null, null))
                .thenReturn(List.of(training));
        when(viewMapper.toView(training)).thenReturn(trainingView);

        List<TrainingView> response = traineeService.getTrainings(request);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().trainingName()).isEqualTo("Morning Session");
    }

    @Test
    @Order(902)
    void getTrainings_withDateFilters_shouldPassFiltersToDao() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        TrainingSearchRequestForTrainee request = new TrainingSearchRequestForTrainee(
                validCredentials.username(), from, to, "Tom.Smith", TrainingType.Type.FITNESS.getName()
        );

        when(traineeRepository.findByUserUsername("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(trainingRepository.findForTrainee("Jack.Black", from, to, "Tom.Smith", TrainingType.Type.FITNESS))
                .thenReturn(List.of());

        List<TrainingView> response = traineeService.getTrainings(request);

        assertThat(response).isEmpty();
        verify(trainingRepository).findForTrainee("Jack.Black", from, to, "Tom.Smith", TrainingType.Type.FITNESS);
    }
}
