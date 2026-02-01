package dev.ilkersahin.java.spring.gym.service.util;

import dev.ilkersahin.java.spring.gym.dto.view.TraineeView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.model.Training;
import org.springframework.stereotype.Service;

@Service
public class ViewMapper {
    public TraineeView toView(Trainee trainee) {
        return new TraineeView(
                trainee.getUser().getUsername(),
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getUser().isActive(),
                trainee.getDateOfBirth(),
                trainee.getAddress()
        );
    }

    public TrainerView toView(Trainer trainer) {
        return new TrainerView(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getUser().isActive(),
                trainer.getSpecialization().getTrainingTypeName()
        );
    }

    public TrainingView toView(Training training) {
        return new TrainingView(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                training.getTrainingType().toEnum(),
                training.getTrainee().getUser().getUsername(),
                training.getTrainer().getUser().getUsername()
        );
    }
}
