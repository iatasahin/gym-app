package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.view.TrainingTypeView;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Training Types", description = "Training type catalog")
public class TrainingTypeController {
    private static final Logger log = LoggerFactory.getLogger(TrainingTypeController.class);


    // =========================================================================
    // 17. GET TRAINING TYPES (Public)
    // =========================================================================

    @GetMapping
    @Operation(summary = "Get all training types", description = "Public endpoint - no authentication required")
    @ApiResponse(responseCode = "200", description = "List of training types",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TrainingTypeView.class)))
    )
    public ResponseEntity<List<TrainingTypeView>> getTrainingTypes() {
        log.info("Getting all training types");

        List<TrainingTypeView> types = Arrays.stream(TrainingType.Type.values())
                .map(type -> new TrainingTypeView(type.getName(), type.getId()))
                .toList();

        return ResponseEntity.ok(types);
    }
}
