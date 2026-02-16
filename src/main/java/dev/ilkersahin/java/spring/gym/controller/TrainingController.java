package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.request.TrainingCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.response.ErrorResponse;
import dev.ilkersahin.java.spring.gym.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trainings", description = "Training session management")
public class TrainingController {

    private final TrainingService trainingService;


    // =========================================================================
    // 14. ADD TRAINING
    // =========================================================================

    @PostMapping
    @Operation(summary = "Add new training session", description = "Creates a training session between a trainee and trainer")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> addTraining(@Valid @RequestBody TrainingCreateRequest request) {
        log.info("Adding training '{}' for trainee '{}'", request.trainingName(), request.traineeUsername());

        trainingService.createTraining(request);

        return ResponseEntity.ok().build();
    }
}
