package dev.ilkersahin.java.spring.gym.client;

import dev.ilkersahin.java.spring.gym.model.Training;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadNotificationService {

    private static final String WORKLOAD_QUEUE = "workload.queue";
    private static final String TRANSACTION_ID = "transactionId";

    private final JmsTemplate jmsTemplate;

    public void notifyTrainingAdded(Training training) {
        WorkloadRequest request = buildRequest(training, ActionType.ADD);
        log.debug("Sending ADD workload: trainer='{}', date={}, duration={}",
                request.trainerUsername(), request.trainingDate(), request.trainingDuration());
        send(request);
    }

    public void notifyTrainingDeleted(Training training) {
        WorkloadRequest request = buildRequest(training, ActionType.DELETE);
        log.debug("Sending DELETE workload: trainer='{}', date={}, duration={}",
                request.trainerUsername(), request.trainingDate(), request.trainingDuration());
        send(request);
    }

    private void send(WorkloadRequest request) {
        try {
            String transactionId = MDC.get(TRANSACTION_ID);
            jmsTemplate.convertAndSend(WORKLOAD_QUEUE, request, message -> {
                if (transactionId != null) {
                    message.setStringProperty(TRANSACTION_ID, transactionId);
                }
                return message;
            });
        } catch (Exception e) {
            log.error("Failed to send workload message: trainer='{}', action={}. Error: {}",
                    request.trainerUsername(), request.actionType(), e.getMessage());
        }
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
