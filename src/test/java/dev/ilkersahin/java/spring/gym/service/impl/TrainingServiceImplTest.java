package dev.ilkersahin.java.spring.gym.service.impl;


import dev.ilkersahin.java.spring.gym.dao.TraineeDao;
import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.dao.TrainingDao;
import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;
import dev.ilkersahin.java.spring.gym.model.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TrainingServiceImplTest {

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private TrainerDao trainerDao;
    @Mock
    private TrainingDao trainingDao;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private User traineeUser;
    private User trainerUser;
    private Trainee trainee;
    private Trainer trainer;
    private Credentials validCredentials;
    private Credentials wrongPasswordCredentials;
    private Credentials nonExistentCredentials;

    @BeforeEach
    void setUp() {
        traineeUser = new User("Jack", "Black", "Jack.Black", "password123", true);
        trainerUser = new User("Tom", "Smith", "Tom.Smith", "trainerPass", true);

        trainee = new Trainee();
        trainee.setUser(traineeUser);

        trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(TrainingType.fromEnum(TrainingType.Type.FITNESS));

        validCredentials = new Credentials("Jack.Black", "password123");
        wrongPasswordCredentials = new Credentials("Jack.Black", "wrongPassword");
        nonExistentCredentials = new Credentials("Non.Existent", "password");
    }

    // =========================================================================
    // CREATE TRAINING - HAPPY PATH TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void createTraining_withValidRequest_shouldCreateTraining() {
        TrainingCreateRequest request = new TrainingCreateRequest(
                validCredentials,
                "Tom.Smith",
                "Morning Fitness",
                TrainingType.Type.FITNESS,
                LocalDate.of(2024, 6, 15),
                60
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(trainee));
        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));

        trainingService.createTraining(request);

        verify(trainingDao).createTraining(any(Training.class));
    }

    @Test
    @Order(102)
    void createTraining_withValidRequest_shouldPassCorrectDataToDao() {
        TrainingCreateRequest request = new TrainingCreateRequest(
                validCredentials,
                "Tom.Smith",
                "Evening Yoga",
                TrainingType.Type.YOGA,
                LocalDate.of(2024, 7, 20),
                90
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(trainee));
        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));

        ArgumentCaptor<Training> trainingCaptor = ArgumentCaptor.forClass(Training.class);

        trainingService.createTraining(request);

        verify(trainingDao).createTraining(trainingCaptor.capture());

        Training captured = trainingCaptor.getValue();
        assertThat(captured.getTrainee()).isEqualTo(trainee);
        assertThat(captured.getTrainer()).isEqualTo(trainer);
        assertThat(captured.getTrainingName()).isEqualTo("Evening Yoga");
        assertThat(captured.getTrainingDate()).isEqualTo(LocalDate.of(2024, 7, 20));
        assertThat(captured.getTrainingDuration()).isEqualTo(90);
    }

    @Test
    @Order(103)
    void createTraining_withDifferentDurations_shouldSetCorrectDuration() {
        TrainingCreateRequest request = new TrainingCreateRequest(
                validCredentials,
                "Tom.Smith",
                "Quick Session",
                TrainingType.Type.STRETCHING,
                LocalDate.of(2024, 8, 1),
                30
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(trainee));
        when(trainerDao.getTrainer("Tom.Smith")).thenReturn(Optional.of(trainer));

        ArgumentCaptor<Training> trainingCaptor = ArgumentCaptor.forClass(Training.class);

        trainingService.createTraining(request);

        verify(trainingDao).createTraining(trainingCaptor.capture());
        assertThat(trainingCaptor.getValue().getTrainingDuration()).isEqualTo(30);
    }

    // =========================================================================
    // CREATE TRAINING - AUTHENTICATION FAILURE TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void createTraining_withNonExistentTrainee_shouldThrowEntityNotFoundException() {
        TrainingCreateRequest request = new TrainingCreateRequest(
                nonExistentCredentials,
                "Tom.Smith",
                "Training",
                TrainingType.Type.FITNESS,
                LocalDate.now(),
                60
        );

        when(traineeDao.getTrainee("Non.Existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.createTraining(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee not found");

        verify(trainingDao, never()).createTraining(any());
    }

    @Test
    @Order(202)
    void createTraining_withWrongPassword_shouldThrowIllegalArgumentException() {
        TrainingCreateRequest request = new TrainingCreateRequest(
                wrongPasswordCredentials,
                "Tom.Smith",
                "Training",
                TrainingType.Type.FITNESS,
                LocalDate.now(),
                60
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> trainingService.createTraining(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credentials");

        verify(trainingDao, never()).createTraining(any());
    }

    // =========================================================================
    // CREATE TRAINING - TRAINER NOT FOUND TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void createTraining_withNonExistentTrainer_shouldThrowEntityNotFoundException() {
        TrainingCreateRequest request = new TrainingCreateRequest(
                validCredentials,
                "Non.Existent.Trainer",
                "Training",
                TrainingType.Type.FITNESS,
                LocalDate.now(),
                60
        );

        when(traineeDao.getTrainee("Jack.Black")).thenReturn(Optional.of(trainee));
        when(trainerDao.getTrainer("Non.Existent.Trainer")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.createTraining(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found");

        verify(trainingDao, never()).createTraining(any());
    }
}
