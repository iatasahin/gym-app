package dev.ilkersahin.java.spring.gym.api;

import dev.ilkersahin.java.spring.gym.config.SecuredEndpointResponses;
import dev.ilkersahin.java.spring.gym.dto.request.ActivationRequest;
import dev.ilkersahin.java.spring.gym.dto.request.PasswordChangeRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TrainerCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TrainerUpdateRequest;
import dev.ilkersahin.java.spring.gym.dto.response.ErrorResponse;
import dev.ilkersahin.java.spring.gym.dto.response.UserCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerWithListView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainingView;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Trainers", description = "Trainer registration and profile management")
@Timed(
        value = "gym.http.trainer",
        description = "Trainer controller HTTP requests"
)
public interface TrainerApi {

    @PostMapping
    @Operation(
            summary = "Register new trainer",
            description = """
                    Create a new trainer account.
                    
                    **Public endpoint - no authentication required.**
                    
                    Returns auto-generated username and password. Save these credentials to login later.
                    """,
            security = {}
    )
    @ApiResponse(
            responseCode = "201",
            description = "Trainer created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserCreateResponse.class),
                    examples = @ExampleObject(value = """
                            {
                                "username": "Jane.Smith",
                                "password": "xY7zAb3dEf"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation error - request body contains invalid data",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = """
                            {
                                "timestamp": "2024-01-15T10:30:00",
                                "status": 400,
                                "error": "Bad Request",
                                "message": "Validation failed: specialization must not be blank",
                                "path": "/api/v1/trainers"
                            }
                            """)
            )
    )
    ResponseEntity<UserCreateResponse> registerTrainer(
            @Valid @RequestBody TrainerCreateRequest request
    );


    @GetMapping("/{username}")
    @Operation(
            summary = "Get trainer profile",
            description = """
                    Retrieve trainer profile information.
                    
                    **🔒 Requires authentication** - User can only access their own profile.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(
            responseCode = "200",
            description = "Trainer profile",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TrainerWithListView.class),
                    examples = @ExampleObject(value = """
                            {
                                "username": "Jane.Smith",
                                "firstName": "Jane",
                                "lastName": "Smith",
                                "active": true,
                                "specialization": "Fitness",
                                "trainees": [
                                    {
                                        "username": "John.Doe",
                                        "firstName": "John",
                                        "lastName": "Doe"
                                    }
                                ]
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = """
                            {
                                "timestamp": "2024-01-15T10:30:00",
                                "status": 404,
                                "error": "Not Found",
                                "message": "Trainer not found with username 'Unknown.User'",
                                "path": "/api/v1/trainers/Unknown.User"
                            }
                            """)
            )
    )
    @SecuredEndpointResponses
    ResponseEntity<TrainerWithListView> getTrainerProfile(
            @Parameter(
                    description = "User's unique username (format: FirstName.LastName)",
                    example = "Jane.Smith",
                    required = true
            )
            @PathVariable String username
    );


    @PutMapping("/{username}")
    @Operation(
            summary = "Update trainer profile",
            description = """
                    Update trainer profile information.
                    
                    **🔒 Requires authentication** - User can only update their own profile.
                    
                    Note: `specialization` cannot be changed after registration.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(
            responseCode = "200",
            description = "Updated trainer profile",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TrainerWithListView.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainer not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecuredEndpointResponses
    ResponseEntity<TrainerWithListView> updateTrainerProfile(
            @Parameter(
                    description = "User's unique username (format: FirstName.LastName)",
                    example = "Jane.Smith",
                    required = true
            )
            @PathVariable String username,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TrainerUpdateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "username": "Jane.Smith",
                                        "firstName": "Jane",
                                        "lastName": "Smith-Johnson",
                                        "active": true
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody TrainerUpdateRequest request
    );


    @PutMapping("/{username}/password")
    @Operation(
            summary = "Change trainer password",
            description = """
                    Change trainer's password.
                    
                    **🔒 Requires authentication** - User can only change their own password.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(
            responseCode = "400",
            description = "Validation error - invalid old password or weak new password",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = """
                            {
                                "timestamp": "2024-01-15T10:30:00",
                                "status": 400,
                                "error": "Bad Request",
                                "message": "Old password is incorrect",
                                "path": "/api/v1/trainers/Jane.Smith/password"
                            }
                            """)
            )
    )
    @SecuredEndpointResponses
    ResponseEntity<Void> changePassword(
            @Parameter(
                    description = "User's unique username (format: FirstName.LastName)",
                    example = "Jane.Smith",
                    required = true
            )
            @PathVariable String username,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PasswordChangeRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "username": "Jane.Smith",
                                        "oldPassword": "currentPass123",
                                        "newPassword": "newSecurePass456"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody PasswordChangeRequest request
    );


    @PatchMapping("/{username}/status")
    @Operation(
            summary = "Activate or deactivate trainer",
            description = """
                    Change trainer's active status.
                    
                    **🔒 Requires authentication** - User can only change their own status.
                    
                    **Non-idempotent operation**: Returns `409 Conflict` if user is already in the requested state.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Status updated successfully")
    @ApiResponse(
            responseCode = "409",
            description = "User already in requested state",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "alreadyActive",
                                    summary = "Already active",
                                    value = """
                                            {
                                                "timestamp": "2024-01-15T10:30:00",
                                                "status": 409,
                                                "error": "Conflict",
                                                "message": "User 'Jane.Smith' is already active",
                                                "path": "/api/v1/trainers/Jane.Smith/status"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "alreadyInactive",
                                    summary = "Already inactive",
                                    value = """
                                            {
                                                "timestamp": "2024-01-15T10:30:00",
                                                "status": 409,
                                                "error": "Conflict",
                                                "message": "User 'Jane.Smith' is already inactive",
                                                "path": "/api/v1/trainers/Jane.Smith/status"
                                            }
                                            """
                            )
                    }
            )
    )
    @SecuredEndpointResponses
    ResponseEntity<Void> updateActivationStatus(
            @Parameter(
                    description = "User's unique username (format: FirstName.LastName)",
                    example = "Jane.Smith",
                    required = true
            )
            @PathVariable String username,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ActivationRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "activate",
                                            summary = "Activate user",
                                            value = """
                                                    {"username": "Jane.Smith", "active": true}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "deactivate",
                                            summary = "Deactivate user",
                                            value = """
                                                    {"username": "Jane.Smith", "active": false}
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody ActivationRequest request
    );


    @GetMapping("/{username}/trainings")
    @Operation(
            summary = "Get trainer's training sessions",
            description = """
                    Returns training sessions conducted by the trainer with optional filters.
                    
                    **🔒 Requires authentication** - User can only access their own trainings.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(
            responseCode = "200",
            description = "List of trainings",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TrainingView.class)),
                    examples = @ExampleObject(value = """
                            [
                                {
                                    "trainingName": "Morning Cardio Session",
                                    "trainingDate": "2024-01-20",
                                    "durationMinutes": 60,
                                    "trainingType": "Fitness",
                                    "traineeUsername": "John.Doe",
                                    "trainerUsername": "Jane.Smith"
                                },
                                {
                                    "trainingName": "Evening Strength Training",
                                    "trainingDate": "2024-01-21",
                                    "durationMinutes": 90,
                                    "trainingType": "Fitness",
                                    "traineeUsername": "Mike.Johnson",
                                    "trainerUsername": "Jane.Smith"
                                }
                            ]
                            """)
            )
    )
    @SecuredEndpointResponses
    @Timed(
            value = "gym.http.trainer.training.search",
            description = "Training search for Trainer endpoint",
            percentiles = {0.95, 0.99},
            histogram = true
    )
    ResponseEntity<List<TrainingView>> getTrainings(
            @Parameter(
                    description = "User's unique username (format: FirstName.LastName)",
                    example = "Jane.Smith",
                    required = true
            )
            @PathVariable String username,

            @Parameter(description = "Filter trainings from this date (inclusive)", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "Filter trainings until this date (inclusive)", example = "2024-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @Parameter(description = "Filter by trainee username", example = "John.Doe")
            @RequestParam(required = false) String traineeName
    );
}
