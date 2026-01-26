package dev.ilkersahin.java.spring.gym.service.util;

import dev.ilkersahin.java.spring.gym.dto.view.TraineeView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.model.*;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ViewMapperTest {

    private ViewMapper viewMapper;

    @BeforeEach
    void setUp() {
        viewMapper = new ViewMapper();
    }

    // =========================================================================
    // TO TRAINEE VIEW TESTS (100s)
    // =========================================================================

    @Test
    @Order(101)
    void toView_withTrainee_shouldMapAllFields() {
        User user = new User("Jack", "Black", "Jack.Black", "password", true);
        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(LocalDate.of(1990, 5, 15));
        trainee.setAddress("123 Main St");

        TraineeView view = viewMapper.toView(trainee);

        assertThat(view.username()).isEqualTo("Jack.Black");
        assertThat(view.firstName()).isEqualTo("Jack");
        assertThat(view.lastName()).isEqualTo("Black");
        assertThat(view.active()).isTrue();
        assertThat(view.dateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(view.address()).isEqualTo("123 Main St");
    }

    // =========================================================================
    // TO TRAINER VIEW TESTS (200s)
    // =========================================================================

    @Test
    @Order(201)
    void toView_withTrainer_shouldMapAllFields() {
        User user = new User("Tom", "Trainer", "Tom.Trainer", "password", true);
        TrainingType trainingType = TrainingType.fromEnum(TrainingType.Type.FITNESS);
        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(trainingType);

        TrainerView view = viewMapper.toView(trainer);

        assertThat(view.username()).isEqualTo("Tom.Trainer");
        assertThat(view.firstName()).isEqualTo("Tom");
        assertThat(view.lastName()).isEqualTo("Trainer");
        assertThat(view.active()).isTrue();
        assertThat(view.specialization()).isEqualTo("Fitness");
    }

    // =========================================================================
    // TO TRAINING VIEW TESTS (300s)
    // =========================================================================

    @Test
    @Order(301)
    void toView_withTraining_shouldMapAllFields() {
        User traineeUser = new User("Jack", "Black", "Jack.Black", "pass", true);
        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);

        User trainerUser = new User("Tom", "Trainer", "Tom.Trainer", "pass", true);
        TrainingType trainingType = TrainingType.fromEnum(TrainingType.Type.YOGA);
        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(trainingType);

        Training training = new Training(
                trainee,
                trainer,
                "Morning Yoga",
                trainingType,
                LocalDate.of(2024, 6, 15),
                Duration.ofMinutes(60)
        );

        TrainingView view = viewMapper.toView(training);

        assertThat(view.trainingName()).isEqualTo("Morning Yoga");
        assertThat(view.trainingDate()).isEqualTo(LocalDate.of(2024, 6, 15));
        assertThat(view.durationMinutes()).isEqualTo(60);
        assertThat(view.trainingType()).isEqualTo(TrainingType.Type.YOGA);
        assertThat(view.traineeUsername()).isEqualTo("Jack.Black");
        assertThat(view.trainerUsername()).isEqualTo("Tom.Trainer");
    }
}
