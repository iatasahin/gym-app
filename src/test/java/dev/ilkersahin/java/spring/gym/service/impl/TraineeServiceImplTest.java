package dev.ilkersahin.java.spring.gym.service.impl;

import dev.ilkersahin.java.spring.gym.dao.TraineeDao;
import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.dao.TrainingDao;
import dev.ilkersahin.java.spring.gym.dao.UserDao;
import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.*;
import dev.ilkersahin.java.spring.gym.dto.view.TraineeView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.model.*;
import dev.ilkersahin.java.spring.gym.service.util.PasswordGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.UsernameGeneratorService;
import dev.ilkersahin.java.spring.gym.service.util.ViewMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TraineeServiceImplTest {

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private TrainingDao trainingDao;
    @Mock private UserDao userDao;
    @Mock private UsernameGeneratorService usernameGeneratorService;
    @Mock private PasswordGeneratorService passwordGeneratorService;
    @Mock private ViewMapper viewMapper;

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
        when(traineeDao.createTrainee(any(Trainee.class))).thenReturn(activeTrainee);
        when(viewMapper.toView(activeTrainee)).thenReturn(traineeView);

        TraineeCreateResponse response = traineeService.createTrainee(request);

        assertThat(response.trainee().username()).isEqualTo("Jack.Black");
        assertThat(response.password()).isEqualTo("password123");
        verify(userDao).persist(any(User.class));
        verify(traineeDao).createTrainee(any(Trainee.class));
    }

    @Test
    @Order(102)
    void createTrainee_withNullOptionalFields_shouldSucceed() {
        TraineeCreateRequest request = new TraineeCreateRequest(
                "Jack", "Black", null, null
        );

        when(usernameGeneratorService.generateUniqueUsername("Jack", "Black")).thenReturn("Jack.Black");
        when(passwordGeneratorService.generate(10)).thenReturn("password123");
        when(traineeDao.createTrainee(any(Trainee.class))).thenReturn(activeTrainee);
        when(viewMapper.toView(activeTrainee)).thenReturn(traineeView);

        TraineeCreateResponse response = traineeService.createTrainee(request);

        assertThat(response).isNotNull();
        assertThat(response.trainee()).isNotNull();
    }

    // =========================================================================
    // GET TRAINEE TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void getTrainee_withValidCredentials_shouldReturnTrainee() {
        TraineeGetRequest request = new TraineeGetRequest(validCredentials);

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(viewMapper.toView(activeTrainee)).thenReturn(traineeView);

        TraineeGetResponse response = traineeService.getTrainee(request);

        assertThat(response.trainee().username()).isEqualTo("Jack.Black");
        assertThat(response.trainee().firstName()).isEqualTo("Jack");
    }

    // =========================================================================
    // UPDATE TRAINEE TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void updateTrainee_withAllFields_shouldUpdateAllFields() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                validCredentials, "John", "Doe",
                LocalDate.of(1985, 5, 5), "456 Oak St"
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(traineeDao.updateTrainee(activeTrainee)).thenReturn(activeTrainee);

        TraineeUpdateResponse response = traineeService.updateTrainee(request);

        assertThat(response.successful()).isTrue();
        assertThat(activeTrainee.getUser().getFirstName()).isEqualTo("John");
        assertThat(activeTrainee.getUser().getLastName()).isEqualTo("Doe");
        assertThat(activeTrainee.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 5, 5));
        assertThat(activeTrainee.getAddress()).isEqualTo("456 Oak St");
    }

    @Test
    @Order(302)
    void updateTrainee_withOnlyFirstName_shouldUpdateOnlyFirstName() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                validCredentials, "John", null, null, null
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(traineeDao.updateTrainee(activeTrainee)).thenReturn(activeTrainee);

        traineeService.updateTrainee(request);

        assertThat(activeTrainee.getUser().getFirstName()).isEqualTo("John");
        assertThat(activeTrainee.getUser().getLastName()).isEqualTo("Black"); // unchanged
    }

    @Test
    @Order(303)
    void updateTrainee_withOnlyLastName_shouldUpdateOnlyLastName() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                validCredentials, null, "Doe", null, null
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(traineeDao.updateTrainee(activeTrainee)).thenReturn(activeTrainee);

        traineeService.updateTrainee(request);

        assertThat(activeTrainee.getUser().getFirstName()).isEqualTo("Jack"); // unchanged
        assertThat(activeTrainee.getUser().getLastName()).isEqualTo("Doe");
    }

    @Test
    @Order(304)
    void updateTrainee_withOnlyDateOfBirth_shouldUpdateOnlyDateOfBirth() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                validCredentials, null, null, LocalDate.of(1995, 12, 25), null
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(traineeDao.updateTrainee(activeTrainee)).thenReturn(activeTrainee);

        traineeService.updateTrainee(request);

        assertThat(activeTrainee.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 12, 25));
        assertThat(activeTrainee.getAddress()).isEqualTo("123 Main St"); // unchanged
    }

    @Test
    @Order(305)
    void updateTrainee_withOnlyAddress_shouldUpdateOnlyAddress() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                validCredentials, null, null, null, "789 Pine Ave"
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(traineeDao.updateTrainee(activeTrainee)).thenReturn(activeTrainee);

        traineeService.updateTrainee(request);

        assertThat(activeTrainee.getAddress()).isEqualTo("789 Pine Ave");
        assertThat(activeTrainee.getUser().getFirstName()).isEqualTo("Jack"); // unchanged
    }

    @Test
    @Order(306)
    void updateTrainee_withNoFields_shouldNotChangeAnything() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                validCredentials, null, null, null, null
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(traineeDao.updateTrainee(activeTrainee)).thenReturn(activeTrainee);

        TraineeUpdateResponse response = traineeService.updateTrainee(request);

        assertThat(response.successful()).isTrue();
        assertThat(activeTrainee.getUser().getFirstName()).isEqualTo("Jack");
        assertThat(activeTrainee.getUser().getLastName()).isEqualTo("Black");
    }

    // =========================================================================
    // CHANGE PASSWORD TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void changePassword_withValidCredentials_shouldUpdatePassword() {
        TraineePasswordChangeRequest request = new TraineePasswordChangeRequest(
                validCredentials, "newSecurePassword"
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));

        TraineePasswordChangeResponse response = traineeService.changePassword(request);

        assertThat(response.successful()).isTrue();
        assertThat(activeTrainee.getUser().getPassword()).isEqualTo("newSecurePassword");
        verify(userDao).merge(activeTrainee.getUser());
    }

    // =========================================================================
    // ACTIVATE / DEACTIVATE TESTS (500s)
    // =========================================================================

    @Test
    @Order(501)
    void activate_withValidCredentials_shouldSetActiveTrue() {
        activeUser.setActive(false); // start inactive for this test
        ActivationRequest request = new ActivationRequest(
                new Credentials("Jack.Black", "password123")
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));

        ActivationResponse response = traineeService.activate(request);

        assertThat(response.active()).isTrue();
        assertThat(activeTrainee.getUser().isActive()).isTrue();
    }

    @Test
    @Order(502)
    void deactivate_withValidCredentials_shouldSetActiveFalse() {
        ActivationRequest request = new ActivationRequest(validCredentials);

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));

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

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));

        TraineeDeleteResponse response = traineeService.deleteTrainee(request);

        assertThat(response.deleted()).isTrue();
        verify(traineeDao).deleteTrainee("Jack.Black");
    }

    // =========================================================================
    // GET UNASSIGNED TRAINERS TESTS (700s)
    // =========================================================================

    @Test
    @Order(701)
    void getUnassignedTrainers_withValidCredentials_shouldReturnTrainerList() {
        TraineeGetRequest request = new TraineeGetRequest(validCredentials);

        Trainer trainer = new Trainer();
        User trainerUser = new User("Tom", "Smith", "Tom.Smith", "pass", true);
        trainer.setUser(trainerUser);

        TrainerView trainerView = new TrainerView(
                "Tom.Smith", "Tom", "Smith", true, "Fitness"
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(trainerDao.findTrainersNotAssignedToTrainee("Jack.Black")).thenReturn(List.of(trainer));
        when(viewMapper.toView(trainer)).thenReturn(trainerView);

        TrainerListResponse response = traineeService.getUnassignedTrainers(request);

        assertThat(response.trainers()).hasSize(1);
        assertThat(response.trainers().get(0).username()).isEqualTo("Tom.Smith");
    }

    @Test
    @Order(702)
    void getUnassignedTrainers_withNoAvailableTrainers_shouldReturnEmptyList() {
        TraineeGetRequest request = new TraineeGetRequest(validCredentials);

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(trainerDao.findTrainersNotAssignedToTrainee("Jack.Black")).thenReturn(List.of());

        TrainerListResponse response = traineeService.getUnassignedTrainers(request);

        assertThat(response.trainers()).isEmpty();
    }

    // =========================================================================
    // UPDATE TRAINERS TESTS (800s)
    // =========================================================================

    @Test
    @Order(801)
    void updateTrainers_withValidUsernames_shouldUpdateTrainerList() {
        TraineeTrainerUpdateRequest request = new TraineeTrainerUpdateRequest(
                validCredentials, List.of("Tom.Smith", "Jane.Doe")
        );

        Trainer trainer1 = new Trainer();
        Trainer trainer2 = new Trainer();

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(trainerDao.findByUsernames(List.of("Tom.Smith", "Jane.Doe")))
                .thenReturn(List.of(trainer1, trainer2));

        TraineeUpdateResponse response = traineeService.updateTrainers(request);

        assertThat(response.successful()).isTrue();
        verify(traineeDao).updateTrainers("Jack.Black", List.of(trainer1, trainer2));
    }

    @Test
    @Order(802)
    void updateTrainers_withEmptyList_shouldClearTrainers() {
        TraineeTrainerUpdateRequest request = new TraineeTrainerUpdateRequest(
                validCredentials, List.of()
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(trainerDao.findByUsernames(List.of())).thenReturn(List.of());

        TraineeUpdateResponse response = traineeService.updateTrainers(request);

        assertThat(response.successful()).isTrue();
        verify(traineeDao).updateTrainers("Jack.Black", List.of());
    }

    // =========================================================================
    // GET TRAININGS TESTS (900s)
    // =========================================================================

    @Test
    @Order(901)
    void getTrainings_withNoFilters_shouldReturnAllTrainings() {
        TrainingSearchRequestForTrainee request = new TrainingSearchRequestForTrainee(
                validCredentials, null, null, null, null
        );

        Training training = new Training();
        TrainingView trainingView = new TrainingView(
                "Morning Session", LocalDate.now(), 60,
                TrainingType.Type.FITNESS.getName(), "Jack.Black", "Tom.Smith"
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(trainingDao.findForTrainee("Jack.Black", null, null, null, null))
                .thenReturn(List.of(training));
        when(viewMapper.toView(training)).thenReturn(trainingView);

        TrainingSearchResponse response = traineeService.getTrainings(request);

        assertThat(response.trainings()).hasSize(1);
        assertThat(response.trainings().get(0).trainingName()).isEqualTo("Morning Session");
    }

    @Test
    @Order(902)
    void getTrainings_withDateFilters_shouldPassFiltersToDao() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        TrainingSearchRequestForTrainee request = new TrainingSearchRequestForTrainee(
                validCredentials, from, to, "Tom.Smith", TrainingType.Type.FITNESS
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));
        when(trainingDao.findForTrainee("Jack.Black", from, to, "Tom.Smith", TrainingType.Type.FITNESS))
                .thenReturn(List.of());

        TrainingSearchResponse response = traineeService.getTrainings(request);

        assertThat(response.trainings()).isEmpty();
        verify(trainingDao).findForTrainee("Jack.Black", from, to, "Tom.Smith", TrainingType.Type.FITNESS);
    }

    // =========================================================================
    // AUTHENTICATION FAILURE TESTS (1000s)
    // =========================================================================

    @Test
    @Order(1001)
    void getTrainee_withNonExistentUser_shouldThrowEntityNotFoundException() {
        TraineeGetRequest request = new TraineeGetRequest(nonExistentCredentials);

        when(traineeDao.getTrainee("Non.Existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.getTrainee(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee not found");
    }

    @Test
    @Order(1002)
    void getTrainee_withWrongPassword_shouldThrowIllegalArgumentException() {
        TraineeGetRequest request = new TraineeGetRequest(wrongPasswordCredentials);

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(activeTrainee));

        assertThatThrownBy(() -> traineeService.getTrainee(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credentials");
    }

    // =========================================================================
    // VALIDATION TESTS (1100s) — DTO constraint validation
    // =========================================================================

}
