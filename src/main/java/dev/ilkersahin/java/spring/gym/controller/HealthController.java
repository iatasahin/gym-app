package dev.ilkersahin.java.spring.gym.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/health")
    public Map<String, Object> health(){
        return Map.of("status", "UP", "timestamp", LocalDateTime.now());
    }
}
