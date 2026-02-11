package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dao.TraineeDao;
import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.dto.auth.LoginRequest;
import dev.ilkersahin.java.spring.gym.dto.auth.LoginResponse;
import dev.ilkersahin.java.spring.gym.exception.InvalidCredentialsException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.model.User;
import dev.ilkersahin.java.spring.gym.security.JwtService;
import dev.ilkersahin.java.spring.gym.security.Role;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthControllerTest {

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    private User traineeUser;
    private Trainee trainee;
    private User trainerUser;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        traineeUser = new User("John", "Doe", "John.Doe", "password123", true);
        trainee = new Trainee();
        trainee.setUser(traineeUser);

        trainerUser = new User("Jane", "Smith", "Jane.Smith", "trainerPass", true);
        trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecializationType(TrainingType.Type.FITNESS);
    }

    // =========================================================================
    // TRAINEE LOGIN TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void login_withValidTraineeCredentials_shouldReturnToken() {
        LoginRequest request = new LoginRequest("John.Doe", "password123");
        when(traineeDao.getTrainee("John.Doe")).thenReturn(Optional.of(trainee));
        when(jwtService.generateToken("John.Doe", Role.TRAINEE)).thenReturn("traineeToken123");

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isEqualTo("traineeToken123");
        assertThat(response.getBody().username()).isEqualTo("John.Doe");
        assertThat(response.getBody().role()).isEqualTo(Role.TRAINEE);
    }

    @Test
    @Order(102)
    void login_withWrongTraineePassword_shouldTryTrainerThenFail() {
        LoginRequest request = new LoginRequest("John.Doe", "wrongPassword");
        when(traineeDao.getTrainee("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerDao.getTrainer("John.Doe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authController.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid username or password");
    }

    // =========================================================================
    // TRAINER LOGIN TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void login_withValidTrainerCredentials_shouldReturnToken() {
        LoginRequest request = new LoginRequest("Jane.Smith", "trainerPass");
        when(traineeDao.getTrainee("Jane.Smith")).thenReturn(Optional.empty());
        when(trainerDao.getTrainer("Jane.Smith")).thenReturn(Optional.of(trainer));
        when(jwtService.generateToken("Jane.Smith", Role.TRAINER)).thenReturn("trainerToken456");

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isEqualTo("trainerToken456");
        assertThat(response.getBody().username()).isEqualTo("Jane.Smith");
        assertThat(response.getBody().role()).isEqualTo(Role.TRAINER);
    }

    @Test
    @Order(202)
    void login_withWrongTrainerPassword_shouldFail() {
        LoginRequest request = new LoginRequest("Jane.Smith", "wrongPassword");
        when(traineeDao.getTrainee("Jane.Smith")).thenReturn(Optional.empty());
        when(trainerDao.getTrainer("Jane.Smith")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> authController.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    // =========================================================================
    // NON-EXISTENT USER TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void login_withNonExistentUser_shouldFail() {
        LoginRequest request = new LoginRequest("NonExistent", "anyPassword");
        when(traineeDao.getTrainee("NonExistent")).thenReturn(Optional.empty());
        when(trainerDao.getTrainer("NonExistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authController.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid username or password");
    }

    // =========================================================================
    // PRIORITY TESTS (400s) - Trainee checked before Trainer
    // =========================================================================

    @Test
    @Order(401)
    void login_userExistsAsBoth_shouldAuthenticateAsTraineeFirst() {
        // Same username exists as both trainee and trainer (edge case)
        LoginRequest request = new LoginRequest("John.Doe", "password123");
        when(traineeDao.getTrainee("John.Doe")).thenReturn(Optional.of(trainee));
        when(jwtService.generateToken("John.Doe", Role.TRAINEE)).thenReturn("traineeToken");

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertThat(response.getBody().role()).isEqualTo(Role.TRAINEE);
    }
}
