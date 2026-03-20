package dev.ilkersahin.java.spring.gym.workload.service;

import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadRequest;
import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadResponse;

public interface WorkloadService {
    void processWorkload(WorkloadRequest request);
    WorkloadResponse getTrainerWorkload(String username);
}
