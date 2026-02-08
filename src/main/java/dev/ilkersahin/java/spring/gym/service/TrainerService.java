package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.*;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerWithListView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;

import java.util.List;

public interface TrainerService {
    UserCreateResponse createTrainer(TrainerCreateRequest request);
    TrainerWithListView getTrainer(String username);
    TrainerWithListView updateTrainer(TrainerUpdateRequest request);
    Boolean changePassword(PasswordChangeRequest request);
    ActivationResponse activate(ActivationRequest request);
    ActivationResponse deactivate(ActivationRequest request);
    List<TrainingView> getTrainings(TrainingSearchRequestForTrainer request);
}
