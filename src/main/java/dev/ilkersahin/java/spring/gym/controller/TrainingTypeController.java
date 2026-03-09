package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.view.TrainingTypeView;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/training-types")
@Slf4j
@Tag(name = "Training Types", description = "Training type catalog")
@Timed(
        value = "gym.http.training-type",
        description = "Training Type controller HTTP requests"
)
public class TrainingTypeController {

    // =========================================================================
    // 17. GET TRAINING TYPES (Public)
    // =========================================================================

    @GetMapping
    @Operation(
            summary = "Get all training types",
            description = """
            Returns all available training types.
            
            **Public endpoint - no authentication required.**
            
            Training types are predefined and cannot be modified by users.
            Use these values when creating trainers (specialization) or training sessions.
            """,
            security = {}
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of training types",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TrainingTypeView.class)),
                    examples = @ExampleObject(value = """
                [
                    {"trainingTypeId": 1, "trainingType": "Fitness"},
                    {"trainingTypeId": 2, "trainingType": "Yoga"},
                    {"trainingTypeId": 3, "trainingType": "Zumba"},
                    {"trainingTypeId": 4, "trainingType": "Stretching"},
                    {"trainingTypeId": 5, "trainingType": "Resistance"}
                ]
                """)
            )
    )
    public ResponseEntity<List<TrainingTypeView>> getTrainingTypes() {
        log.info("Getting all training types");

        List<TrainingTypeView> types = Arrays.stream(TrainingType.Type.values())
                .map(type -> new TrainingTypeView(type.getName(), type.getId()))
                .toList();

        return ResponseEntity.ok(types);
    }
}
