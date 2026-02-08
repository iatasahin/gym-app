package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.*;
import dev.ilkersahin.java.spring.gym.dto.view.*;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyActiveException;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyInactiveException;
import dev.ilkersahin.java.spring.gym.service.TraineeService;
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
public class TraineeController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(TraineeController.class);

    private final TraineeService traineeService;


    // =========================================================================
    // 1. REGISTRATION (Public)
    // =========================================================================

    @PostMapping
    public ResponseEntity<UserCreateResponse> registerTrainee(@Valid @RequestBody TraineeCreateRequest request) {

        log.info("Registering new trainee: {} {}", request.firstName(), request.lastName());

        UserCreateResponse response = traineeService.createTrainee(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================================================================
    // 5. GET PROFILE
    // =========================================================================

    @GetMapping("/{username}")
    public ResponseEntity<TraineeWithListView> getTraineeProfile(@PathVariable String username, HttpServletRequest request) {

        verifyUserAccess(request, username);
        log.info("Getting profile for trainee '{}'", username);

        TraineeWithListView response = traineeService.getTrainee(username);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 6. UPDATE PROFILE
    // =========================================================================

    @PutMapping("/{username}")
    public ResponseEntity<TraineeWithListView> updateTraineeProfile(
            @PathVariable String username,
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
    public ResponseEntity<Void> deleteTrainee(@PathVariable String username, HttpServletRequest request) {

        verifyUserAccess(request, username);
        log.warn("Deleting trainee '{}'", username);

        traineeService.deleteTrainee(username);
        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // 4. CHANGE PASSWORD
    // =========================================================================

    @PutMapping("/{username}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String username,
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
    public ResponseEntity<Void> updateActivationStatus(
            @PathVariable String username,
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
    public ResponseEntity<List<TrainerInfo>> getUnassignedTrainers(
            @PathVariable String username,
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
    public ResponseEntity<List<TrainerInfo>> updateTrainers(
            @PathVariable String username,
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
    public ResponseEntity<List<TrainingView>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String trainerName,
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
