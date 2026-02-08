package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.*;
import dev.ilkersahin.java.spring.gym.dto.view.TraineeWithListView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerInfo;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;

import java.util.List;

public interface TraineeService {
    UserCreateResponse createTrainee(TraineeCreateRequest request);
    TraineeWithListView getTrainee(String username);
    TraineeWithListView updateTrainee(TraineeUpdateRequest request);
    Boolean changePassword(PasswordChangeRequest request);
    ActivationResponse activate(ActivationRequest request);
    ActivationResponse deactivate(ActivationRequest request);
    Boolean deleteTrainee(String username);
    List<TrainerInfo> getUnassignedTrainers(String username);
    List<TrainerInfo> updateTrainers(TraineeTrainerListUpdateRequest request);
    List<TrainingView> getTrainings(TrainingSearchRequestForTrainee request);
}
