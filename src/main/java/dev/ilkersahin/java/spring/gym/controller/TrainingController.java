package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.response.ErrorResponse;
import dev.ilkersahin.java.spring.gym.service.TrainingService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Trainings", description = "Training session management")
@Timed(
        value = "gym.http.training",
        description = "Training controller HTTP requests"
)
public class TrainingController {

    private final TrainingService trainingService;


    // =========================================================================
    // 14. ADD TRAINING
    // =========================================================================

    @PostMapping
    @Operation(
            summary = "Add new training session",
            description = """
                    Create a training session between a trainee and trainer.
                    
                    **🔒 Requires authentication**
                    
                    The training type must match one of the available training types.
                    Both trainee and trainer must exist and be active.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Training created successfully")
    @ApiResponse(
            responseCode = "400",
            description = "Validation error - request body contains invalid data",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "invalidTrainingType",
                                    summary = "Invalid training type",
                                    value = """
                            {
                                "timestamp": "2024-01-15T10:30:00",
                                "status": 400,
                                "error": "Bad Request",
                                "message": "Invalid training type: 'InvalidType'. Valid types: Fitness, Yoga, Zumba, Stretching, Resistance",
                                "path": "/api/v1/trainings"
                            }
                            """
                            ),
                            @ExampleObject(
                                    name = "invalidDuration",
                                    summary = "Invalid duration",
                                    value = """
                            {
                                "timestamp": "2024-01-15T10:30:00",
                                "status": 400,
                                "error": "Bad Request",
                                "message": "Validation failed: durationMinutes must be at least 1",
                                "path": "/api/v1/trainings"
                            }
                            """
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Missing or invalid JWT token",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-01-15T10:30:00",
                        "status": 401,
                        "error": "Unauthorized",
                        "message": "Missing or invalid Authorization header",
                        "path": "/api/v1/trainings"
                    }
                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Trainee or trainer not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "traineeNotFound",
                                    summary = "Trainee not found",
                                    value = """
                            {
                                "timestamp": "2024-01-15T10:30:00",
                                "status": 404,
                                "error": "Not Found",
                                "message": "Trainee not found with username 'Unknown.Trainee'",
                                "path": "/api/v1/trainings"
                            }
                            """
                            ),
                            @ExampleObject(
                                    name = "trainerNotFound",
                                    summary = "Trainer not found",
                                    value = """
                            {
                                "timestamp": "2024-01-15T10:30:00",
                                "status": 404,
                                "error": "Not Found",
                                "message": "Trainer not found with username 'Unknown.Trainer'",
                                "path": "/api/v1/trainings"
                            }
                            """
                            )
                    }
            )
    )
    public ResponseEntity<Void> addTraining(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Training session details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TrainingCreateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "fitnessTraining",
                                            summary = "Fitness training session",
                                            value = """
                            {
                                "traineeUsername": "John.Doe",
                                "trainerUsername": "Jane.Smith",
                                "trainingName": "Morning Cardio Session",
                                "trainingType": "Fitness",
                                "trainingDate": "2024-01-20",
                                "durationMinutes": 60
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "yogaTraining",
                                            summary = "Yoga training session",
                                            value = """
                            {
                                "traineeUsername": "John.Doe",
                                "trainerUsername": "Emily.Brown",
                                "trainingName": "Relaxing Yoga Session",
                                "trainingType": "Yoga",
                                "trainingDate": "2024-01-22",
                                "durationMinutes": 45
                            }
                            """
                                    )
                            }
                    )
            )
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
    @Operation(
            summary = "Delete training session",
            description = """
                    Delete a training session by its ID.
                    
                    **🔒 Requires authentication**
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Training deleted successfully")
    @ApiResponse(responseCode = "404", description = "Training not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deleteTraining(
            @Parameter(description = "Training session UUID", required = true)
            @PathVariable UUID trainingId
    ) {
        log.info("Deleting training with id '{}'", trainingId);

        trainingService.deleteTraining(trainingId);

        return ResponseEntity.ok().build();
    }
}
