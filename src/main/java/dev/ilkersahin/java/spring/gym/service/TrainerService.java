package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.*;

public interface TrainerService {
    TrainerCreateResponse createTrainer(TrainerCreateRequest request);
    TrainerGetResponse getTrainer(TrainerGetRequest request);
    TrainerUpdateResponse updateTrainer(TrainerUpdateRequest request);
    TrainerPasswordChangeResponse changePassword(TrainerPasswordChangeRequest request);
    ActivationResponse activate(ActivationRequest request);
    ActivationResponse deactivate(ActivationRequest request);
    TrainingSearchResponse getTrainings(TrainingSearchRequestForTrainer request);
}
