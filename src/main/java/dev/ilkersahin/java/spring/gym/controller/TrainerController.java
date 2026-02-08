package dev.ilkersahin.java.spring.gym.controller;

import dev.ilkersahin.java.spring.gym.dto.request.*;
import dev.ilkersahin.java.spring.gym.dto.response.TrainerCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.response.UserCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerWithListView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyActiveException;
import dev.ilkersahin.java.spring.gym.exception.UserAlreadyInactiveException;
import dev.ilkersahin.java.spring.gym.service.TrainerService;
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
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
public class TrainerController extends BaseController {
    public static final Logger log = LoggerFactory.getLogger(TrainerController.class);

    private final TrainerService trainerService;


    // =========================================================================
    // 2. REGISTRATION (Public)
    // =========================================================================

    @PostMapping
    public ResponseEntity<UserCreateResponse> registerTrainer(
            @Valid @RequestBody TrainerCreateRequest request) {

        log.info("Registering new trainer: {} {}", request.firstName(), request.lastName());

        UserCreateResponse response = trainerService.createTrainer(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================================================
    // 8. GET PROFILE
    // =========================================================================

    @GetMapping("/{username}")
    public ResponseEntity<TrainerWithListView> getTrainerProfile(
            @PathVariable String username,
            HttpServletRequest request) {

        verifyUserAccess(request, username);
        log.info("Getting profile for trainer '{}'", username);

        TrainerWithListView response = trainerService.getTrainer(username);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 9. UPDATE PROFILE
    // =========================================================================

    @PutMapping("/{username}")
    public ResponseEntity<TrainerWithListView> updateTrainerProfile(
            @PathVariable String username,
            @Valid @RequestBody TrainerUpdateRequest request,
            HttpServletRequest httpRequest) {

        verifyUserAccess(httpRequest, username);
        log.info("Updating profile for trainer '{}'", username);

        TrainerWithListView updated = trainerService.updateTrainer(request);
        return ResponseEntity.ok(updated);
    }

    // =========================================================================
    // 4. CHANGE PASSWORD
    // =========================================================================

    @PutMapping("/{username}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String username,
            @Valid @RequestBody PasswordChangeRequest request,
            HttpServletRequest httpRequest) {

        verifyUserAccess(httpRequest, username);
        log.info("Changing password for trainer '{}'", username);

        trainerService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    // =========================================================================
    // 16. ACTIVATE/DEACTIVATE (Non-idempotent)
    // =========================================================================

    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> updateActivationStatus(
            @PathVariable String username,
            @Valid @RequestBody ActivationRequest request,
            HttpServletRequest httpRequest) {

        verifyUserAccess(httpRequest, username);
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
    public ResponseEntity<List<TrainingView>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String traineeName,
            HttpServletRequest request) {

        verifyUserAccess(request, username);
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
