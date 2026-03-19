package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.api.TraineeApi;
import dev.ilkersahin.java.spring.gym.dto.request.ActivationRequest;
import dev.ilkersahin.java.spring.gym.dto.request.PasswordChangeRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeTrainerListUpdateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeUpdateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TrainingSearchRequestForTrainee;
import dev.ilkersahin.java.spring.gym.dto.response.UserCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.view.TraineeWithListView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerInfo;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyActiveException;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyInactiveException;
import dev.ilkersahin.java.spring.gym.security.selfservice.SelfService;
import dev.ilkersahin.java.spring.gym.service.TraineeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainees")
@RequiredArgsConstructor
@Slf4j
public class TraineeController implements TraineeApi {

    private final TraineeService traineeService;

    // =========================================================================
    // 1. REGISTRATION (Public)
    // =========================================================================

    @PostMapping
    @Override
    public ResponseEntity<UserCreateResponse> registerTrainee(
            @Valid @RequestBody TraineeCreateRequest request
    ) {

        log.info("Registering new trainee: {} {}", request.firstName(), request.lastName());

        UserCreateResponse response = traineeService.createTrainee(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================================================================
    // 5. GET PROFILE
    // =========================================================================

    @GetMapping("/{username}")
    @SelfService
    @Override
    public ResponseEntity<TraineeWithListView> getTraineeProfile(
            @PathVariable String username
    ) {
        log.info("Getting profile for trainee '{}'", username);

        TraineeWithListView response = traineeService.getTrainee(username);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 6. UPDATE PROFILE
    // =========================================================================

    @PutMapping("/{username}")
    @SelfService
    @Override
    public ResponseEntity<TraineeWithListView> updateTraineeProfile(
            @PathVariable String username,
            @Valid @RequestBody TraineeUpdateRequest request
    ) {
        log.info("Updating profile for trainee '{}'", username);

        TraineeWithListView response = traineeService.updateTrainee(request);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 7. DELETE PROFILE
    // =========================================================================

    @DeleteMapping("/{username}")
    @SelfService
    @Override
    public ResponseEntity<Void> deleteTrainee(
            @PathVariable String username
    ) {
        log.warn("Deleting trainee '{}'", username);

        traineeService.deleteTrainee(username);
        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // 4. CHANGE PASSWORD
    // =========================================================================

    @PutMapping("/{username}/password")
    @SelfService
    @Override
    public ResponseEntity<Void> changePassword(
            @PathVariable String username,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        log.info("Changing password for trainee '{}'", username);

        traineeService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // 15. ACTIVATE/DEACTIVATE (Non-idempotent)
    // =========================================================================

    @PatchMapping("/{username}/status")
    @SelfService
    @Override
    public ResponseEntity<Void> updateActivationStatus(
            @PathVariable String username,
            @Valid @RequestBody ActivationRequest request
    ) {
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
    @SelfService
    @Override
    public ResponseEntity<List<TrainerInfo>> getUnassignedTrainers(
            @PathVariable String username
    ) {
        log.info("Getting unassigned trainers for trainee '{}'", username);

        List<TrainerInfo> response = traineeService.getUnassignedTrainers(username);

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 11. UPDATE TRAINER LIST
    // =========================================================================

    @PutMapping("/{username}/trainers")
    @SelfService
    @Override
    public ResponseEntity<List<TrainerInfo>> updateTrainers(
            @PathVariable String username,
            @Valid @RequestBody TraineeTrainerListUpdateRequest request
    ) {
        log.info("Updating trainers for trainee '{}'", username);

        List<TrainerInfo> response = traineeService.updateTrainers(request);

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 12. GET TRAININGS
    // =========================================================================

    @GetMapping("/{username}/trainings")
    @SelfService
    @Override
    public ResponseEntity<List<TrainingView>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType
    ) {
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
