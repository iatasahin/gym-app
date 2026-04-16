package dev.ilkersahin.java.spring.gym.workload.controller;

import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadResponse;
import dev.ilkersahin.java.spring.gym.workload.service.WorkloadService;
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

    @GetMapping("/{username}")
    public ResponseEntity<WorkloadResponse> getTrainerWorkload(@PathVariable String username) {
        log.info("Fetching workload for trainer '{}'", username);

        WorkloadResponse response = workloadService.getTrainerWorkload(username);

        log.info("Returning workload for trainer '{}': {} year(s)",
                username, response.years().size());

        return ResponseEntity.ok(response);
    }
}
