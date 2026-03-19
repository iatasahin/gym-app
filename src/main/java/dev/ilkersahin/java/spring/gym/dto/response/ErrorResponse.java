package dev.ilkersahin.java.spring.gym.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Standard error response format")
public record ErrorResponse(
        @Schema(description = "When the error occurred", example = "2024-01-15T10:30:00", format = "date-time")
        LocalDateTime timestamp,

        @Schema(description = "HTTP status code", example = "401")
        int status,

        @Schema(description = "HTTP status text", example = "Unauthorized")
        String error,

        @Schema(description = "Detailed error message", example = "Invalid username or password")
        String message,

        @Schema(description = "Request path that caused the error", example = "/api/v1/auth/login")
        String path
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path);
    }
}
