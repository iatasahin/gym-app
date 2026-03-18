package dev.ilkersahin.java.spring.gym.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkloadClientFallbackFactory implements FallbackFactory<WorkloadClient> {
    @Override
    public WorkloadClient create(Throwable cause) {
        return request -> log.error(
                "Workload service unavailable — trainer='{}', action={}, date={}, duration={}. Cause: {}",
                request.trainerUsername(),
                request.actionType(),
                request.trainingDate(),
                request.trainingDuration(),
                cause.getMessage()
        );
    }
}
