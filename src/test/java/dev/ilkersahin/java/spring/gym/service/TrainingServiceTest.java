package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dao.TrainingDAO;
import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingServiceTest {

    private TrainingService service;
    private TrainingDAO trainingDAO;

    private final UUID traineeId = UUID.randomUUID();
    private final UUID trainerId = UUID.randomUUID();
    private final LocalDate date = LocalDate.of(2024,8,24);


    @BeforeEach
    void setUp() {
        trainingDAO = mock(TrainingDAO.class);
        service = new TrainingService();
        service.setTrainingDAO(trainingDAO);
    }

    private Training sample() {
        return new Training(
                traineeId,
                trainerId,
                "Workout",
                TrainingType.FITNESS,
                date,
                Duration.ofMinutes(60)
        );
    }

    @Test
    void createTrainingDelegatesToDao() {
        Training t = sample();
        when(trainingDAO.createTraining(t)).thenReturn(t);

        Training saved = service.createTraining(t);

        assertThat(saved).isSameAs(t);
    }

    @Test
    void getTrainingDelegatesToDao() {
        when(trainingDAO.getTraining(traineeId, trainerId, date))
                .thenReturn(List.of(sample()));

        assertThat(service.getTraining(traineeId, trainerId, date))
                .hasSize(1);
    }

    @Test
    void getAllTrainingsDelegatesToDao() {
        when(trainingDAO.getAllTrainings()).thenReturn(List.of(sample(), sample()));

        assertThat(service.getAllTrainings()).hasSize(2);
    }
}
