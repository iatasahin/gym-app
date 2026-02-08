package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;
import dev.ilkersahin.java.spring.gym.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
public class TrainingController {
    private static final Logger log = LoggerFactory.getLogger(TrainingController.class);

    private final TrainingService trainingService;


    // =========================================================================
    // 14. ADD TRAINING
    // =========================================================================

    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody TrainingCreateRequest request) {
        log.info("Adding training '{}' for trainee '{}'", request.trainingName(), request.traineeUsername());

        trainingService.createTraining(request);

        return ResponseEntity.ok().build();
    }
}
