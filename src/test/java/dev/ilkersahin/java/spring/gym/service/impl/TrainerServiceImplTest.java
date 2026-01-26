package dev.ilkersahin.java.spring.gym.service.impl;


import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.dao.TrainingDao;
import dev.ilkersahin.java.spring.gym.dao.UserDao;
import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.*;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.model.User;
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
class TrainerServiceImplTest {

    @Mock private TrainerDao trainerDao;
    @Mock private TrainingDao trainingDao;
    @Mock private UserDao userDao;
    @Mock private UsernameGeneratorService usernameGeneratorService;
    @Mock private PasswordGeneratorService passwordGeneratorService;
    @Mock private ViewMapper viewMapper;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private User activeUser;
    private Trainer trainer;
    private Credentials validCredentials;
    private Credentials wrongPasswordCredentials;
    private Credentials nonExistentCredentials;
    private TrainerView trainerView;

    @BeforeEach
    void setUp() {
        activeUser = new User("Tom", "Smith", "Tom.Smith", "password123", true);

        trainer = new Trainer();
        trainer.setUser(activeUser);
        trainer.setSpecialization(TrainingType.fromEnum(TrainingType.Type.FITNESS));

        validCredentials = new Credentials("Tom.Smith", "password123");
        wrongPasswordCredentials = new Credentials("Tom.Smith", "wrongPassword");
        nonExistentCredentials = new Credentials("Non.Existent", "password");

        trainerView = new TrainerView(
                "Tom.Smith", "Tom", "Smith", true, "Fitness"
        );
    }

    // =========================================================================
    // CREATE TRAINER TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void createTrainer_withValidRequest_shouldReturnResponseWithUsernameAndPassword() {
        TrainerCreateRequest request = new TrainerCreateRequest(
                "Tom", "Smith", TrainingType.Type.FITNESS
        );

        when(usernameGeneratorService.generateUniqueUsername("Tom", "Smith")).thenReturn("Tom.Smith");
        when(passwordGeneratorService.generate(10)).thenReturn("password123");
        when(trainerDao.createTrainer(any(Trainer.class))).thenReturn(trainer);
        when(viewMapper.toView(trainer)).thenReturn(trainerView);

        TrainerCreateResponse response = trainerService.createTrainer(request);

        assertThat(response.trainer().username()).isEqualTo("Tom.Smith");
        assertThat(response.password()).isEqualTo("password123");
        verify(userDao).persist(any(User.class));
        verify(trainerDao).createTrainer(any(Trainer.class));
    }

    @Test
    @Order(102)
    void createTrainer_withDifferentSpecialization_shouldSetCorrectSpecialization() {
        TrainerCreateRequest request = new TrainerCreateRequest(
                "Jane", "Doe", TrainingType.Type.RESISTANCE
        );

        User user = new User("Jane", "Doe", "Jane.Doe", "pass123", true);
        Trainer resistanceTrainer = new Trainer();
        resistanceTrainer.setUser(user);
        resistanceTrainer.setSpecialization(TrainingType.fromEnum(TrainingType.Type.RESISTANCE));

        TrainerView resistanceView = new TrainerView(
                "Jane.Doe", "Jane", "Doe", true, "Resistance"
        );

        when(usernameGeneratorService.generateUniqueUsername("Jane", "Doe")).thenReturn("Jane.Doe");
        when(passwordGeneratorService.generate(10)).thenReturn("pass123");
        when(trainerDao.createTrainer(any(Trainer.class))).thenReturn(resistanceTrainer);
        when(viewMapper.toView(resistanceTrainer)).thenReturn(resistanceView);

        TrainerCreateResponse response = trainerService.createTrainer(request);

        assertThat(response.trainer().specialization()).isEqualTo("Resistance");
    }

    // =========================================================================
    // GET TRAINER TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void getTrainer_withValidCredentials_shouldReturnTrainer() {
        TrainerGetRequest request = new TrainerGetRequest(validCredentials);

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(viewMapper.toView(trainer)).thenReturn(trainerView);

        TrainerGetResponse response = trainerService.getTrainer(request);

        assertThat(response.trainer().username()).isEqualTo("Tom.Smith");
        assertThat(response.trainer().firstName()).isEqualTo("Tom");
        assertThat(response.trainer().specialization()).isEqualTo("Fitness");
    }

    // =========================================================================
    // UPDATE TRAINER TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void updateTrainer_withAllFields_shouldUpdateAllFields() {
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                validCredentials, "John", "Doe", TrainingType.Type.YOGA
        );

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainerDao.updateTrainer(trainer)).thenReturn(trainer);

        TrainerUpdateResponse response = trainerService.updateTrainer(request);

        assertThat(response.successful()).isTrue();
        assertThat(trainer.getUser().getFirstName()).isEqualTo("John");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Doe");
        assertThat(trainer.getSpecialization().getTrainingTypeName())
                .isEqualTo(TrainingType.Type.YOGA.getName());
    }

    @Test
    @Order(302)
    void updateTrainer_withOnlyFirstName_shouldUpdateOnlyFirstName() {
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                validCredentials, "John", null, null
        );

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainerDao.updateTrainer(trainer)).thenReturn(trainer);

        trainerService.updateTrainer(request);

        assertThat(trainer.getUser().getFirstName()).isEqualTo("John");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Smith"); // unchanged
    }

    @Test
    @Order(303)
    void updateTrainer_withOnlyLastName_shouldUpdateOnlyLastName() {
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                validCredentials, null, "Doe", null
        );

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainerDao.updateTrainer(trainer)).thenReturn(trainer);

        trainerService.updateTrainer(request);

        assertThat(trainer.getUser().getFirstName()).isEqualTo("Tom"); // unchanged
        assertThat(trainer.getUser().getLastName()).isEqualTo("Doe");
    }

    @Test
    @Order(304)
    void updateTrainer_withOnlySpecialization_shouldUpdateOnlySpecialization() {
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                validCredentials, null, null, TrainingType.Type.STRETCHING
        );

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainerDao.updateTrainer(trainer)).thenReturn(trainer);

        trainerService.updateTrainer(request);

        assertThat(trainer.getUser().getFirstName()).isEqualTo("Tom"); // unchanged
        assertThat(trainer.getSpecialization().getTrainingTypeName())
                .isEqualTo(TrainingType.Type.STRETCHING.getName());
    }

    @Test
    @Order(305)
    void updateTrainer_withNoFields_shouldNotChangeAnything() {
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                validCredentials, null, null, null
        );

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainerDao.updateTrainer(trainer)).thenReturn(trainer);

        TrainerUpdateResponse response = trainerService.updateTrainer(request);

        assertThat(response.successful()).isTrue();
        assertThat(trainer.getUser().getFirstName()).isEqualTo("Tom");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Smith");
    }

    // =========================================================================
    // CHANGE PASSWORD TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void changePassword_withValidCredentials_shouldUpdatePassword() {
        TrainerPasswordChangeRequest request = new TrainerPasswordChangeRequest(
                validCredentials, "newSecurePassword"
        );

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));

        TrainerPasswordChangeResponse response = trainerService.changePassword(request);

        assertThat(response.successful()).isTrue();
        assertThat(trainer.getUser().getPassword()).isEqualTo("newSecurePassword");
        verify(userDao).merge(trainer.getUser());
    }

    // =========================================================================
    // ACTIVATE / DEACTIVATE TESTS (500s)
    // =========================================================================

    @Test
    @Order(501)
    void activate_withValidCredentials_shouldSetActiveTrue() {
        activeUser.setActive(false);
        ActivationRequest request = new ActivationRequest(validCredentials);

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));

        ActivationResponse response = trainerService.activate(request);

        assertThat(response.active()).isTrue();
        assertThat(trainer.getUser().isActive()).isTrue();
    }

    @Test
    @Order(502)
    void deactivate_withValidCredentials_shouldSetActiveFalse() {
        ActivationRequest request = new ActivationRequest(validCredentials);

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));

        ActivationResponse response = trainerService.deactivate(request);

        assertThat(response.active()).isFalse();
        assertThat(trainer.getUser().isActive()).isFalse();
    }

    // =========================================================================
    // GET TRAININGS TESTS (600s)
    // =========================================================================

    @Test
    @Order(601)
    void getTrainings_withNoFilters_shouldReturnAllTrainings() {
        TrainingSearchRequestForTrainer request = new TrainingSearchRequestForTrainer(
                validCredentials, null, null, null
        );

        Training training = new Training();
        TrainingView trainingView = new TrainingView(
                "Morning Session", LocalDate.now(), 60,
                TrainingType.Type.FITNESS, "Jack.Black", "Tom.Smith"
        );

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainingDao.findForTrainer("Tom.Smith", null, null, null))
                .thenReturn(List.of(training));
        when(viewMapper.toView(training)).thenReturn(trainingView);

        TrainingSearchResponse response = trainerService.getTrainings(request);

        assertThat(response.trainings()).hasSize(1);
        assertThat(response.trainings().getFirst().trainerUsername()).isEqualTo("Tom.Smith");
    }

    @Test
    @Order(602)
    void getTrainings_withFilters_shouldPassFiltersToDao() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        TrainingSearchRequestForTrainer request = new TrainingSearchRequestForTrainer(
                validCredentials, from, to, "Jack.Black"
        );

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainingDao.findForTrainer("Tom.Smith", from, to, "Jack.Black"))
                .thenReturn(List.of());

        TrainingSearchResponse response = trainerService.getTrainings(request);

        assertThat(response.trainings()).isEmpty();
        verify(trainingDao).findForTrainer("Tom.Smith", from, to, "Jack.Black");
    }

    @Test
    @Order(603)
    void getTrainings_withNoResults_shouldReturnEmptyList() {
        TrainingSearchRequestForTrainer request = new TrainingSearchRequestForTrainer(
                validCredentials, null, null, null
        );

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainingDao.findForTrainer("Tom.Smith", null, null, null))
                .thenReturn(List.of());

        TrainingSearchResponse response = trainerService.getTrainings(request);

        assertThat(response.trainings()).isEmpty();
    }

    // =========================================================================
    // AUTHENTICATION FAILURE TESTS (700s)
    // =========================================================================

    @Test
    @Order(701)
    void getTrainer_withNonExistentUser_shouldThrowEntityNotFoundException() {
        TrainerGetRequest request = new TrainerGetRequest(nonExistentCredentials);

        when(trainerDao.getTrainer("Non.Existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getTrainer(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found");
    }

    @Test
    @Order(702)
    void getTrainer_withWrongPassword_shouldThrowIllegalArgumentException() {
        TrainerGetRequest request = new TrainerGetRequest(wrongPasswordCredentials);

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.getTrainer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @Order(703)
    void updateTrainer_withWrongPassword_shouldThrowIllegalArgumentException() {
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                wrongPasswordCredentials, "John", null, null
        );

        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.updateTrainer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credentials");
    }
}
