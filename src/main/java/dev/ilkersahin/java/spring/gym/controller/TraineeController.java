package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.*;
import dev.ilkersahin.java.spring.gym.dto.view.*;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyActiveException;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyInactiveException;
import dev.ilkersahin.java.spring.gym.security.InjectRandomNumber;
import dev.ilkersahin.java.spring.gym.service.TraineeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainees")
@RequiredArgsConstructor
@Tag(name = "Trainees", description = "Trainee registration and profile management")
public class TraineeController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(TraineeController.class);

    private final TraineeService traineeService;

    @InjectRandomNumber(min = -10, max = 10)
    private int randomNumber; // For demonstration of custom annotation - can be used for logging or other purposes

    @PostConstruct
    public void init() {
        log.info("TraineeController initialized with random number: {}", randomNumber);
    }


    // =========================================================================
    // 1. REGISTRATION (Public)
    // =========================================================================

    @PostMapping
    @Operation(summary = "Register new trainee", description = "Public endpoint - no authentication required")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainee created successfully",
                    content = @Content(schema = @Schema(implementation = UserCreateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserCreateResponse> registerTrainee(@Valid @RequestBody TraineeCreateRequest request) {

        log.info("Registering new trainee: {} {}", request.firstName(), request.lastName());

        UserCreateResponse response = traineeService.createTrainee(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================================================================
    // 5. GET PROFILE
    // =========================================================================

    @GetMapping("/{username}")
    @Operation(summary = "Get trainee profile", description = "Requires authentication - user can only access own profile")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee profile",
                    content = @Content(schema = @Schema(implementation = TraineeWithListView.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TraineeWithListView> getTraineeProfile(
            @Parameter(description = "Trainee username") @PathVariable String username,
            HttpServletRequest request
    ) {

        verifyUserAccess(request, username);
        log.info("Getting profile for trainee '{}'", username);

        TraineeWithListView response = traineeService.getTrainee(username);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 6. UPDATE PROFILE
    // =========================================================================

    @PutMapping("/{username}")
    @Operation(summary = "Update trainee profile")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated trainee profile",
                    content = @Content(schema = @Schema(implementation = TraineeWithListView.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TraineeWithListView> updateTraineeProfile(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody TraineeUpdateRequest request,
            HttpServletRequest httpRequest
    ) {

        verifyUserAccess(httpRequest, username);
        log.info("Updating profile for trainee '{}'", username);

        TraineeWithListView response = traineeService.updateTrainee(request);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 7. DELETE PROFILE
    // =========================================================================

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainee", description = "Permanently removes trainee and all associated data")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteTrainee(
            @Parameter(description = "Trainee username") @PathVariable String username,
            HttpServletRequest request
    ) {

        verifyUserAccess(request, username);
        log.warn("Deleting trainee '{}'", username);

        traineeService.deleteTrainee(username);
        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // 4. CHANGE PASSWORD
    // =========================================================================

    @PutMapping("/{username}/password")
    @Operation(summary = "Change trainee password")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody PasswordChangeRequest request,
            HttpServletRequest httpRequest
    ) {

        verifyUserAccess(httpRequest, username);
        log.info("Changing password for trainee '{}'", username);

        traineeService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // 15. ACTIVATE/DEACTIVATE (Non-idempotent)
    // =========================================================================

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate trainee",
            description = "Non-idempotent operation. Returns 409 if already in requested state.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "User already in requested state",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> updateActivationStatus(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody ActivationRequest request,
            HttpServletRequest httpRequest
    ) {

        verifyUserAccess(httpRequest, username);
        log.info("Updating activation status for trainee '{}' to {}", username, request.active());

        // Check current status for non-idempotent behavior
        TraineeWithListView current = traineeService.getTrainee(username);

        if (request.active() && current.active()) {
            throw new UserAlreadyActiveException(username);
        }
        if (!request.active() && !current.active()) {
            throw new UserAlreadyInactiveException(username);
        }

        if (request.active()) {
            traineeService.activate(request);
        } else {
            traineeService.deactivate(request);
        }

        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // 10. GET UNASSIGNED TRAINERS
    // =========================================================================

    @GetMapping("/{username}/trainers/unassigned")
    @Operation(summary = "Get unassigned trainers", description = "Returns trainers not currently assigned to this trainee")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of unassigned trainers",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TrainerInfo.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TrainerInfo>> getUnassignedTrainers(
            @Parameter(description = "Trainee username") @PathVariable String username,
            HttpServletRequest request) {

        verifyUserAccess(request, username);
        log.info("Getting unassigned trainers for trainee '{}'", username);

        List<TrainerInfo> response = traineeService.getUnassignedTrainers(username);

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 11. UPDATE TRAINER LIST
    // =========================================================================

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update trainer list", description = "Replace trainee's assigned trainers with the provided list")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated trainer list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TrainerInfo.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TrainerInfo>> updateTrainers(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody TraineeTrainerListUpdateRequest request,
            HttpServletRequest httpRequest) {

        verifyUserAccess(httpRequest, username);
        log.info("Updating trainers for trainee '{}'", username);

        List<TrainerInfo> response = traineeService.updateTrainers(request);

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 12. GET TRAININGS
    // =========================================================================

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainee's trainings", description = "Returns training sessions with optional filters")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of trainings",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TrainingView.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TrainingView>> getTrainings(
            @Parameter(description = "Trainee username")
            @PathVariable String username,
            @Parameter(description = "Filter: start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Filter: end date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Filter: trainer name")
            @RequestParam(required = false) String trainerName,
            @Parameter(description = "Filter: training type")
            @RequestParam(required = false) String trainingType,
            HttpServletRequest request
    ) {

        verifyUserAccess(request, username);
        log.info("Getting trainings for trainee '{}'", username);

        TrainingSearchRequestForTrainee searchRequest = new TrainingSearchRequestForTrainee(
                username,
                fromDate,
                toDate,
                trainerName,
                trainingType
        );

        List<TrainingView> response = traineeService.getTrainings(searchRequest);
        return ResponseEntity.ok(response);
    }
}
