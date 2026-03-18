package dev.ilkersahin.java.spring.gym.client;

import dev.ilkersahin.java.spring.gym.model.Training;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadNotificationService {
    private final WorkloadClient workloadClient;

    public void notifyTrainingAdded(Training training) {
        WorkloadRequest request = buildRequest(training, ActionType.ADD);
        log.debug("Sending ADD workload: trainer='{}', date={}, duration={}",
                request.trainerUsername(), request.trainingDate(), request.trainingDuration());
        workloadClient.sendWorkloadAction(request);
    }

    public void notifyTrainingDeleted(Training training) {
        WorkloadRequest request = buildRequest(training, ActionType.DELETE);
        log.debug("Sending DELETE workload: trainer='{}', date={}, duration={}",
                request.trainerUsername(), request.trainingDate(), request.trainingDuration());
        workloadClient.sendWorkloadAction(request);
    }

    private WorkloadRequest buildRequest(Training training, ActionType actionType) {
        return new WorkloadRequest(
                training.getTrainer().getUsername(),
                training.getTrainer().getFirstName(),
                training.getTrainer().getLastName(),
                training.getTrainer().isActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType
        );
    }
}
