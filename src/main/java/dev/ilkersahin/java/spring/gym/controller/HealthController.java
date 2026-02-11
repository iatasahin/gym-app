package dev.ilkersahin.java.spring.gym.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Tag(name = "Health", description = "Application health check")
public class HealthController {
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns application health status")
    @ApiResponse(responseCode = "200", description = "Application is healthy")
    public Map<String, Object> health(){
        return Map.of("status", "UP", "timestamp", LocalDateTime.now());
    }
}
