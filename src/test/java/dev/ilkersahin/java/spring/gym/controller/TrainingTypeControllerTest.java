package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.view.TrainingTypeView;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainingTypeControllerTest {
    private final TrainingTypeController controller = new TrainingTypeController();

    @Test
    void getTrainingTypes_shouldReturnAllTypes() {
        ResponseEntity<List<TrainingTypeView>> response = controller.getTrainingTypes();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(TrainingType.Type.values().length);
    }

    @Test
    void getTrainingTypes_shouldContainExpectedTypes() {
        ResponseEntity<List<TrainingTypeView>> response = controller.getTrainingTypes();

        List<String> typeNames = response.getBody().stream()
                .map(TrainingTypeView::trainingType)
                .toList();

        assertThat(typeNames).contains("Fitness", "Yoga", "Zumba", "Stretching", "Resistance");
    }

    @Test
    void getTrainingTypes_shouldHaveCorrectIds() {
        ResponseEntity<List<TrainingTypeView>> response = controller.getTrainingTypes();

        response.getBody().forEach(type -> {
            assertThat(type.trainingTypeId()).isNotNull();
            assertThat(type.trainingType()).isNotNull();
        });
    }
}
