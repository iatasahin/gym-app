package dev.ilkersahin.java.spring.gym.service.impl;

import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import dev.ilkersahin.java.spring.gym.dto.request.ActivationRequest;
import dev.ilkersahin.java.spring.gym.dto.request.PasswordChangeRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TrainerCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TrainerUpdateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TrainingSearchRequestForTrainer;
import dev.ilkersahin.java.spring.gym.dto.response.ActivationResponse;
import dev.ilkersahin.java.spring.gym.dto.response.UserCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerWithListView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.exception.InvalidCredentialsException;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import dev.ilkersahin.java.spring.gym.model.User;
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
class TrainerServiceImplTest {

    @Mock private TrainerRepository trainerRepository;
    @Mock private TrainingRepository trainingRepository;
    @Mock private UserRepository userRepository;
    @Mock private UsernameGeneratorService usernameGeneratorService;
    @Mock private PasswordGeneratorService passwordGeneratorService;
    @Mock private ViewMapper viewMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private User activeUser;
    private Trainer trainer;
    private Credentials validCredentials;

    @BeforeEach
    void setUp() {
        activeUser = new User("Tom", "Smith", "Tom.Smith", "password123", true);

        trainer = new Trainer();
        trainer.setUser(activeUser);
        trainer.setSpecialization(TrainingType.fromEnum(TrainingType.Type.FITNESS));

        validCredentials = new Credentials("Tom.Smith", "password123");
    }

    // =========================================================================
    // CREATE TRAINER TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void createTrainer_withValidRequest_shouldReturnResponseWithUsernameAndPassword() {
        TrainerCreateRequest request = new TrainerCreateRequest(
                "Tom", "Smith", TrainingType.Type.FITNESS.getName()
        );

        when(usernameGeneratorService.generateUniqueUsername("Tom", "Smith")).thenReturn("Tom.Smith");
        when(passwordGeneratorService.generate(10)).thenReturn("password123");
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        UserCreateResponse response = trainerService.createTrainer(request);

        assertThat(response.username()).isEqualTo("Tom.Smith");
        assertThat(response.password()).isEqualTo("password123");
        verify(userRepository).save(any(User.class));
        verify(trainerRepository).save(any(Trainer.class));
    }

    // =========================================================================
    // GET TRAINER TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void getTrainer_withValidCredentials_shouldReturnTrainer() {
        when(trainerRepository.findByUserUsername("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.findAssignedTrainees("Tom.Smith")).thenReturn(List.of());

        TrainerWithListView response = trainerService.getTrainer(validCredentials.username());

        assertThat(response.username()).isEqualTo("Tom.Smith");
        assertThat(response.firstName()).isEqualTo("Tom");
        assertThat(response.specialization()).isEqualTo("Fitness");
        assertThat(response.trainees()).isNotNull();
        assertThat(response.trainees()).isEmpty();
    }

    // =========================================================================
    // UPDATE TRAINER TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void updateTrainer_withAllFields_shouldUpdateAllFields() {
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                validCredentials.username(), "John", "Doe",
                true, TrainingType.Type.YOGA.getName()
        );

        when(trainerRepository.findByUserUsername("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        TrainerWithListView response = trainerService.updateTrainer(request);

        assertThat(response.username()).isEqualTo(validCredentials.username());
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(trainer.getUser().getFirstName()).isEqualTo("John");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Doe");
        assertThat(trainer.getSpecialization().getType().getName())
                .isEqualTo(TrainingType.Type.YOGA.getName());
    }

    @Test
    @Order(302)
    void updateTrainer_withOnlyFirstName_shouldUpdateOnlyFirstName() {
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                validCredentials.username(), "John", null, null, null
        );

        when(trainerRepository.findByUserUsername("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.updateTrainer(request);

        assertThat(trainer.getUser().getFirstName()).isEqualTo("John");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Smith"); // unchanged
    }

    @Test
    @Order(303)
    void updateTrainer_withOnlyLastName_shouldUpdateOnlyLastName() {
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                validCredentials.username(), null, "Doe", null, null
        );

        when(trainerRepository.findByUserUsername("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.updateTrainer(request);

        assertThat(trainer.getUser().getFirstName()).isEqualTo("Tom"); // unchanged
        assertThat(trainer.getUser().getLastName()).isEqualTo("Doe");
    }

    @Test
    @Order(304)
    void updateTrainer_withOnlySpecialization_shouldUpdateOnlySpecialization() {
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                validCredentials.username(), null, null, null, TrainingType.Type.STRETCHING.getName()
        );

        when(trainerRepository.findByUserUsername("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        trainerService.updateTrainer(request);

        assertThat(trainer.getUser().getFirstName()).isEqualTo("Tom"); // unchanged
        assertThat(trainer.getSpecialization().getType().getName())
                .isEqualTo(TrainingType.Type.STRETCHING.getName());
    }

    @Test
    @Order(305)
    void updateTrainer_withNoFields_shouldNotChangeAnything() {
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                validCredentials.username(), null, null, null, null
        );

        when(trainerRepository.findByUserUsername("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        TrainerWithListView response = trainerService.updateTrainer(request);

        assertThat(trainer.getUser().getFirstName()).isEqualTo("Tom");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Smith");
    }

    // =========================================================================
    // CHANGE PASSWORD TESTS (400s)
    // =========================================================================

    @Test
    @Order(401)
    void changePassword_withValidCredentials_shouldUpdatePassword() {
        PasswordChangeRequest request = new PasswordChangeRequest(
                validCredentials.username(), validCredentials.password(), "newSecurePassword"
        );

        when(trainerRepository.findByUserUsername("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches(validCredentials.password(), trainer.getUser().getPassword()))
                .thenReturn(true);
        when(passwordEncoder.encode("newSecurePassword"))
                .thenReturn("$2a$10$hashedNewPassword");

        Boolean response = trainerService.changePassword(request);

        assertThat(response).isTrue();
        assertThat(trainer.getUser().getPassword()).isEqualTo("$2a$10$hashedNewPassword");
        verify(userRepository).save(trainer.getUser());
        verify(passwordEncoder).encode("newSecurePassword");
    }

    @Test
    @Order(402)
    void changePassword_withInvalidOldPassword_shouldThrowException() {
        PasswordChangeRequest request = new PasswordChangeRequest(
                validCredentials.username(), "wrongOldPassword", "newSecurePassword"
        );

        when(trainerRepository.findByUserUsername("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches("wrongOldPassword", trainer.getUser().getPassword()))
                .thenReturn(false);

        assertThatThrownBy(() -> trainerService.changePassword(request))
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
        activeUser.setActive(false);
        ActivationRequest request = new ActivationRequest(validCredentials.username(), true);

        when(trainerRepository.findByUserUsername("Tom.Smith")).thenReturn(Optional.of(trainer));

        ActivationResponse response = trainerService.activate(request);

        assertThat(response.active()).isTrue();
        assertThat(trainer.getUser().isActive()).isTrue();
    }

    @Test
    @Order(502)
    void deactivate_withValidCredentials_shouldSetActiveFalse() {
        ActivationRequest request = new ActivationRequest(validCredentials.username(), false);

        when(trainerRepository.findByUserUsername("Tom.Smith")).thenReturn(Optional.of(trainer));

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
                validCredentials.username(), null, null, null
        );

        Training training = new Training();
        TrainingView trainingView = new TrainingView(
                "Morning Session", LocalDate.now(), 60,
                TrainingType.Type.FITNESS.getName(), "Jack.Black", "Tom.Smith"
        );

        when(trainerRepository.findByUserUsername("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainingRepository.findForTrainer("Tom.Smith", null, null, null))
                .thenReturn(List.of(training));
        when(viewMapper.toView(training)).thenReturn(trainingView);

        List<TrainingView> response = trainerService.getTrainings(request);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().trainerUsername()).isEqualTo("Tom.Smith");
    }

    @Test
    @Order(602)
    void getTrainings_withFilters_shouldPassFiltersToDao() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        TrainingSearchRequestForTrainer request = new TrainingSearchRequestForTrainer(
                validCredentials.username(), from, to, "Jack.Black"
        );

        when(trainerRepository.findByUserUsername("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainingRepository.findForTrainer("Tom.Smith", from, to, "Jack.Black"))
                .thenReturn(List.of());

        List<TrainingView> response = trainerService.getTrainings(request);

        assertThat(response).isEmpty();
        verify(trainingRepository).findForTrainer("Tom.Smith", from, to, "Jack.Black");
    }

    @Test
    @Order(603)
    void getTrainings_withNoResults_shouldReturnEmptyList() {
        TrainingSearchRequestForTrainer request = new TrainingSearchRequestForTrainer(
                validCredentials.username(), null, null, null
        );

        when(trainerRepository.findByUserUsername("Tom.Smith")).thenReturn(Optional.of(trainer));
        when(trainingRepository.findForTrainer("Tom.Smith", null, null, null))
                .thenReturn(List.of());

        List<TrainingView> response = trainerService.getTrainings(request);

        assertThat(response).isEmpty();
    }
}
