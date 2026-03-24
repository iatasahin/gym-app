package dev.ilkersahin.java.spring.gym.workload.listener;

import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadRequest;
import dev.ilkersahin.java.spring.gym.workload.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadMessageListener {

    private static final String WORKLOAD_QUEUE = "workload.queue";
    private static final String TRANSACTION_ID = "transactionId";

    private final WorkloadService workloadService;

    @JmsListener(destination = WORKLOAD_QUEUE)
    public void onWorkloadMessage(
            WorkloadRequest request,
            @Header(name = TRANSACTION_ID, required = false) String transactionId
    ) {
        MDC.put(TRANSACTION_ID,
                transactionId != null ? transactionId : UUID.randomUUID().toString().substring(0, 8)
        );
        try {
            log.info("Received workload message: trainer='{}', action={}, date={}, duration={}",
                    request.trainerUsername(), request.actionType(),
                    request.trainingDate(), request.trainingDuration());
            validate(request);
            workloadService.processWorkload(request);

            log.info("Workload message processed: trainer='{}', action={}",
                    request.trainerUsername(), request.actionType());
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }

    private void validate(WorkloadRequest request) {
        if (request.trainerUsername() == null || request.trainerUsername().isBlank()) {
            throw new IllegalArgumentException("Missing required field: trainerUsername");
        }
        if (request.actionType() == null) {
            throw new IllegalArgumentException("Missing required field: actionType");
        }
        if (request.trainingDate() == null) {
            throw new IllegalArgumentException("Missing required field: trainingDate");
        }
        if (request.trainingDuration() <= 0) {
            throw new IllegalArgumentException("trainingDuration must be positive");
        }
    }
}
