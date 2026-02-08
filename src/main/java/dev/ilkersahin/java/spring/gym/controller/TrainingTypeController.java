package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.view.TrainingTypeView;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/training-types")
public class TrainingTypeController {
    private static final Logger log = LoggerFactory.getLogger(TrainingTypeController.class);


    // =========================================================================
    // 17. GET TRAINING TYPES (Public)
    // =========================================================================

    @GetMapping
    public ResponseEntity<List<TrainingTypeView>> getTrainingTypes() {
        log.info("Getting all training types");

        List<TrainingTypeView> types = Arrays.stream(TrainingType.Type.values())
                .map(type -> new TrainingTypeView(type.getName(), type.getId()))
                .toList();

        return ResponseEntity.ok(types);
    }
}
