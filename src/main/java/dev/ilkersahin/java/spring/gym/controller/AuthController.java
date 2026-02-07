package dev.ilkersahin.java.spring.gym.controller;


import dev.ilkersahin.java.spring.gym.dao.TraineeDao;
import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.dto.auth.LoginRequest;
import dev.ilkersahin.java.spring.gym.dto.auth.LoginResponse;
import dev.ilkersahin.java.spring.gym.exception.InvalidCredentialsException;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import dev.ilkersahin.java.spring.gym.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request){

        log.info("Login attempt for user '{}'", request.username());

        // Try to authenticate as Trainee
        Optional<Trainee> traineeOpt = traineeDao.getTrainee(request.username());
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            if (trainee.getUser().getPassword().equals(request.password())) {
                String token = jwtService.generateToken(request.username(), "TRAINEE");
                log.info("Trainee '{}' logged in successfully", request.username());
                return ResponseEntity.ok(new LoginResponse(token, request.username(), "TRAINEE"));
            }
        }

        // Try to authenticate as Trainer
        Optional<Trainer> trainerOpt = trainerDao.getTrainer(request.username());
        if (trainerOpt.isPresent()) {
            Trainer trainer = trainerOpt.get();
            if (trainer.getUser().getPassword().equals(request.password())) {
                String token = jwtService.generateToken(request.username(), "TRAINER");
                log.info("Trainer '{}' logged in successfully", request.username());
                return ResponseEntity.ok(new LoginResponse(token, request.username(), "TRAINER"));
            }
        }

        log.warn("Failed login attempt for user '{}'", request.username());
        throw new InvalidCredentialsException("Invalid username or password");
    }
}
