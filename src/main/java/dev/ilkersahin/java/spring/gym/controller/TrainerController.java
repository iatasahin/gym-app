package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.api.TrainerApi;
import dev.ilkersahin.java.spring.gym.dto.request.ActivationRequest;
import dev.ilkersahin.java.spring.gym.dto.request.PasswordChangeRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TrainerCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TrainerUpdateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TrainingSearchRequestForTrainer;
import dev.ilkersahin.java.spring.gym.dto.response.UserCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerWithListView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyActiveException;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyInactiveException;
import dev.ilkersahin.java.spring.gym.security.selfservice.SelfService;
import dev.ilkersahin.java.spring.gym.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
@Slf4j
public class TrainerController implements TrainerApi {

    private final TrainerService trainerService;

    // =========================================================================
    // 2. REGISTRATION (Public)
    // =========================================================================

    @PostMapping
    public ResponseEntity<UserCreateResponse> registerTrainer(
            @Valid @RequestBody TrainerCreateRequest request
    ) {
        log.info("Registering new trainer: {} {}", request.firstName(), request.lastName());

        UserCreateResponse response = trainerService.createTrainer(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================================================
    // 8. GET PROFILE
    // =========================================================================

    @GetMapping("/{username}")
    @SelfService
    public ResponseEntity<TrainerWithListView> getTrainerProfile(
            @PathVariable String username
    ) {
        log.info("Getting profile for trainer '{}'", username);

        TrainerWithListView response = trainerService.getTrainer(username);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 9. UPDATE PROFILE
    // =========================================================================

    @PutMapping("/{username}")
    @SelfService
    public ResponseEntity<TrainerWithListView> updateTrainerProfile(
            @PathVariable String username,
            @Valid @RequestBody TrainerUpdateRequest request
    ) {
        log.info("Updating profile for trainer '{}'", username);

        TrainerWithListView updated = trainerService.updateTrainer(request);
        return ResponseEntity.ok(updated);
    }

    // =========================================================================
    // 4. CHANGE PASSWORD
    // =========================================================================

    @PutMapping("/{username}/password")
    @SelfService
    public ResponseEntity<Void> changePassword(
            @PathVariable String username,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        log.info("Changing password for trainer '{}'", username);

        trainerService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // 16. ACTIVATE/DEACTIVATE (Non-idempotent)
    // =========================================================================

    @PatchMapping("/{username}/status")
    @SelfService
    public ResponseEntity<Void> updateActivationStatus(
            @PathVariable String username,
            @Valid @RequestBody ActivationRequest request
    ) {
        log.info("Updating activation status for trainer '{}' to {}", username, request.active());

        // Check current status for non-idempotent behavior
        TrainerWithListView current = trainerService.getTrainer(username);

        if (request.active() && current.active()) {
            throw new UserAlreadyActiveException(username);
        }
        if (!request.active() && !current.active()) {
            throw new UserAlreadyInactiveException(username);
        }

        if (request.active()) {
            trainerService.activate(request);
        } else {
            trainerService.deactivate(request);
        }

        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // 13. GET TRAININGS
    // =========================================================================

    @GetMapping("/{username}/trainings")
    @SelfService
    public ResponseEntity<List<TrainingView>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String traineeName
    ) {
        log.info("Getting trainings for trainer '{}'", username);

        TrainingSearchRequestForTrainer searchRequest = new TrainingSearchRequestForTrainer(
                username,
                fromDate,
                toDate,
                traineeName
        );

        List<TrainingView> response = trainerService.getTrainings(searchRequest);
        return ResponseEntity.ok(response);
    }
}
