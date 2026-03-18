package dev.ilkersahin.java.spring.gym.workload.controller;

import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadRequest;
import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadResponse;
import dev.ilkersahin.java.spring.gym.workload.service.WorkloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workload")
@RequiredArgsConstructor
@Slf4j
public class WorkloadController {
    private final WorkloadService workloadService;

    @PostMapping
    public ResponseEntity<Void> processWorkload(@Valid @RequestBody WorkloadRequest request) {
        log.info("Received workload request: trainer='{}', action={}, date={}, duration={}",
                request.trainerUsername(), request.actionType(),
                request.trainingDate(), request.trainingDuration());

        workloadService.processWorkload(request);

        log.info("Workload processed successfully: trainer='{}', action={}",
                request.trainerUsername(), request.actionType());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<WorkloadResponse> getTrainerWorkload(@PathVariable String username) {
        log.info("Fetching workload for trainer '{}'", username);

        WorkloadResponse response = workloadService.getTrainerWorkload(username);

        log.info("Returning workload for trainer '{}': {} year(s)",
                username, response.years().size());

        return ResponseEntity.ok(response);
    }
}
