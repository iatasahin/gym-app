package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;
import dev.ilkersahin.java.spring.gym.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock private TrainingService trainingService;

    @InjectMocks
    private TrainingController controller;

    @Test
    void addTraining_withValidRequest_shouldCallServiceAndReturn200() {
        TrainingCreateRequest request = new TrainingCreateRequest(
                "John.Doe",
                "Jane.Smith",
                "Morning Cardio",
                "FITNESS",
                LocalDate.now(),
                60
        );

        ResponseEntity<Void> response = controller.addTraining(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(trainingService).createTraining(request);
    }
}
