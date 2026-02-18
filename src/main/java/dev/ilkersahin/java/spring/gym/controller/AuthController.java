package dev.ilkersahin.java.spring.gym.controller;


import dev.ilkersahin.java.spring.gym.dao.TraineeDao;
import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.dto.auth.LoginRequest;
import dev.ilkersahin.java.spring.gym.dto.auth.LoginResponse;
import dev.ilkersahin.java.spring.gym.dto.response.ErrorResponse;
import dev.ilkersahin.java.spring.gym.exception.InvalidCredentialsException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.security.JwtService;
import dev.ilkersahin.java.spring.gym.security.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Login and token management")
public class AuthController {

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and receive JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        log.info("Login attempt for user '{}'", request.username());

        // Try to authenticate as Trainee
        Optional<Trainee> traineeOpt = traineeDao.getTrainee(request.username());
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            if (trainee.getUser().getPassword().equals(request.password())) {
                String token = jwtService.generateToken(request.username(), Role.TRAINEE);
                log.info("Trainee '{}' logged in successfully", request.username());
                return ResponseEntity.ok(new LoginResponse(token, request.username(), Role.TRAINEE));
            }
        }

        // Try to authenticate as Trainer
        Optional<Trainer> trainerOpt = trainerDao.getTrainer(request.username());
        if (trainerOpt.isPresent()) {
            Trainer trainer = trainerOpt.get();
            if (trainer.getUser().getPassword().equals(request.password())) {
                String token = jwtService.generateToken(request.username(), Role.TRAINER);
                log.info("Trainer '{}' logged in successfully", request.username());
                return ResponseEntity.ok(new LoginResponse(token, request.username(), Role.TRAINER));
            }
        }

        log.warn("Failed login attempt for user '{}'", request.username());
        throw new InvalidCredentialsException("Invalid username or password");
    }
}
