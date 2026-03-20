package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.api.TrainingApi;
import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;
import dev.ilkersahin.java.spring.gym.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
@Slf4j
public class TrainingController implements TrainingApi {

    private final TrainingService trainingService;


    // =========================================================================
    // 14. ADD TRAINING
    // =========================================================================

    @PostMapping
    @Override
    public ResponseEntity<Void> addTraining(
            @Valid @RequestBody TrainingCreateRequest request
    ) {
        log.info("Adding training '{}' for trainee '{}'", request.trainingName(), request.traineeUsername());

        trainingService.createTraining(request);

        return ResponseEntity.ok().build();
    }


    // =========================================================================
    // DELETE TRAINING
    // =========================================================================

    @DeleteMapping("/{trainingId}")
    @Override
    public ResponseEntity<Void> deleteTraining(
            @PathVariable UUID trainingId
    ) {
        log.info("Deleting training with id '{}'", trainingId);

        trainingService.deleteTraining(trainingId);

        return ResponseEntity.ok().build();
    }
}
