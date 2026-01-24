package dev.ilkersahin.java.spring.gym.service;

import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.*;

public interface TraineeService {
    TraineeCreateResponse createTrainee(TraineeCreateRequest request);
    TraineeGetResponse getTrainee(TraineeGetRequest request);
    TraineeUpdateResponse updateTrainee(TraineeUpdateRequest request);
    TraineePasswordChangeResponse changePassword(TraineePasswordChangeRequest request);
    ActivationResponse activate(ActivationRequest request);
    ActivationResponse deactivate(ActivationRequest request);
    TraineeDeleteResponse deleteTrainee(TraineeDeleteRequest request);
    TrainerListResponse getUnassignedTrainers(TraineeGetRequest request);
    TraineeUpdateResponse updateTrainers(TraineeTrainerUpdateRequest request);
    TrainingSearchResponse getTrainings(TrainingSearchRequestForTrainee request);
}
