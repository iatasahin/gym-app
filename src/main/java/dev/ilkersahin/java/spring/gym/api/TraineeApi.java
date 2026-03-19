package dev.ilkersahin.java.spring.gym.api;

import dev.ilkersahin.java.spring.gym.config.SecuredEndpointResponses;
import dev.ilkersahin.java.spring.gym.dto.request.ActivationRequest;
import dev.ilkersahin.java.spring.gym.dto.request.PasswordChangeRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeCreateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeTrainerListUpdateRequest;
import dev.ilkersahin.java.spring.gym.dto.request.TraineeUpdateRequest;
import dev.ilkersahin.java.spring.gym.dto.response.ErrorResponse;
import dev.ilkersahin.java.spring.gym.dto.response.UserCreateResponse;
import dev.ilkersahin.java.spring.gym.dto.view.TraineeWithListView;
import dev.ilkersahin.java.spring.gym.dto.view.TrainerInfo;
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

@Tag(name = "Trainees", description = "Trainee registration and profile management")
@Timed(
        value = "gym.http.trainee",
        description = "Trainee controller HTTP requests"
)
public interface TraineeApi {
    @PostMapping
    @Operation(
            summary = "Register new trainee",
            description = """
                    Create a new trainee account.
                    
                    **Public endpoint - no authentication required.**
                    
                    Returns auto-generated username and password. Save these credentials to login later.
                    """,
            security = {}  // Override: no security
    )
    @ApiResponse(
            responseCode = "201",
            description = "Trainee created successfully",
            content = @Content(
                    schema = @Schema(implementation = UserCreateResponse.class),
                    examples = @ExampleObject(value = """
                            {"username": "John.Doe", "password": "aB3dEf9xYz"}
                            """)
            )
    )
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<UserCreateResponse> registerTrainee(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = @ExampleObject(value = """
                                    {
                                        "firstName": "John",
                                        "lastName": "Doe",
                                        "dateOfBirth": "1990-05-15",
                                        "address": "123 Main St, New York"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody TraineeCreateRequest request);


    @GetMapping("/{username}")
    @Operation(
            summary = "Get trainee profile",
            description = """
                    Retrieve trainee profile information.
                    
                    **🔒 Requires authentication** - User can only access their own profile.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Trainee profile",
            content = @Content(schema = @Schema(implementation = TraineeWithListView.class)))
    @ApiResponse(responseCode = "404", description = "Trainee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecuredEndpointResponses
    ResponseEntity<TraineeWithListView> getTraineeProfile(
            @Parameter(
                    description = "User's unique username (format: FirstName.LastName)",
                    example = "John.Doe",
                    required = true
            )
            @PathVariable String username);


    @PutMapping("/{username}")
    @Operation(summary = "Update trainee profile")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = """
            Update trainee profile information.
            
            **🔒 Requires authentication** - User can only update their own profile.
            """,
            content = @Content(schema = @Schema(implementation = TraineeWithListView.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Trainee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecuredEndpointResponses
    ResponseEntity<TraineeWithListView> updateTraineeProfile(
            @Parameter(description = "User's unique username", example = "John.Doe")
            @PathVariable String username,
            @Valid @RequestBody TraineeUpdateRequest request
    );

    @DeleteMapping("/{username}")
    @Operation(
            summary = "Delete trainee",
            description = """
                    Permanently delete trainee and all associated data.
                    
                    **🔒 Requires authentication** - User can only delete their own account.
                    
                    ⚠️ **This action is irreversible.**
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Trainee deleted successfully")
    @ApiResponse(responseCode = "404", description = "Trainee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecuredEndpointResponses
    ResponseEntity<Void> deleteTrainee(
            @Parameter(description = "User's unique username", example = "John.Doe")
            @PathVariable String username
    );


    @PutMapping("/{username}/password")
    @Operation(
            summary = "Change trainee password",
            description = """
                    Change trainee's password.
                    
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
                                "path": "/api/v1/trainees/John.Doe/password"
                            }
                            """)
            )
    )
    @SecuredEndpointResponses
    ResponseEntity<Void> changePassword(
            @Parameter(description = "User's unique username (format: FirstName.LastName)", example = "John.Doe", required = true)
            @PathVariable String username,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PasswordChangeRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "username": "John.Doe",
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
            summary = "Activate or deactivate trainee",
            description = """
                    Change trainee's active status.
                    
                    **Non-idempotent operation**: Returns `409 Conflict` if already in requested state.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Status updated successfully")
    @ApiResponse(
            responseCode = "409",
            description = "User already in requested state",
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "alreadyActive",
                                    summary = "Already active",
                                    value = """
                                            {
                                                "status": 409,
                                                "error": "Conflict",
                                                "message": "User 'John.Doe' is already active"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "alreadyInactive",
                                    summary = "Already inactive",
                                    value = """
                                            {
                                                "status": 409,
                                                "error": "Conflict",
                                                "message": "User 'John.Doe' is already inactive"
                                            }
                                            """
                            )
                    }
            )
    )
    @SecuredEndpointResponses
    ResponseEntity<Void> updateActivationStatus(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = {
                                    @ExampleObject(name = "activate", summary = "Activate user",
                                            value = """
                                                    {"username": "John.Doe", "active": true}
                                                    """),
                                    @ExampleObject(name = "deactivate", summary = "Deactivate user",
                                            value = """
                                                    {"username": "John.Doe", "active": false}
                                                    """)
                            }
                    )
            )
            @Valid @RequestBody ActivationRequest request
    );


    @GetMapping("/{username}/trainers/unassigned")
    @Operation(
            summary = "Get trainers not assigned to trainee",
            description = """
                    Returns list of active trainers not currently assigned to this trainee.
                    
                    **🔒 Requires authentication** - User can only access their own data.
                    
                    Use this to find available trainers that can be added to the trainee's trainer list.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "List of unassigned trainers",
            content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = TrainerInfo.class)),
                    examples = @ExampleObject(value = """
                            [
                                {
                                    "username": "Emily.Brown",
                                    "firstName": "Emily",
                                    "lastName": "Brown",
                                    "specialization": "Yoga"
                                },
                                {
                                    "username": "Mike.Wilson",
                                    "firstName": "Mike",
                                    "lastName": "Wilson",
                                    "specialization": "Resistance"
                                }
                            ]
                            """
                    )
            )
    )
    @ApiResponse(responseCode = "404", description = "Trainee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecuredEndpointResponses
    ResponseEntity<List<TrainerInfo>> getUnassignedTrainers(
            @Parameter(description = "User's unique username (format: FirstName.LastName)", example = "John.Doe", required = true)
            @PathVariable String username
    );


    @PutMapping("/{username}/trainers")
    @Operation(
            summary = "Update trainee's trainer list",
            description = """
                    Replace trainee's assigned trainers with the provided list.
                    
                    **🔒 Requires authentication** - User can only modify their own trainer list.
                    
                    This is a **replacement** operation - the existing trainer list will be completely 
                    replaced with the provided list. To add a trainer, include all existing trainers 
                    plus the new one.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(
            responseCode = "200",
            description = "Updated trainer list",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TrainerInfo.class)),
                    examples = @ExampleObject(value = """
                            [
                                {
                                    "username": "Jane.Smith",
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "specialization": "Fitness"
                                },
                                {
                                    "username": "Bob.Wilson",
                                    "firstName": "Bob",
                                    "lastName": "Wilson",
                                    "specialization": "Yoga"
                                }
                            ]
                            """)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation error - trainer not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = """
                            {
                                "timestamp": "2024-01-15T10:30:00",
                                "status": 400,
                                "error": "Bad Request",
                                "message": "Trainer not found: 'Unknown.Trainer'",
                                "path": "/api/v1/trainees/John.Doe/trainers"
                            }
                            """)
            )
    )
    @SecuredEndpointResponses
    ResponseEntity<List<TrainerInfo>> updateTrainers(
            @Parameter(
                    description = "User's unique username (format: FirstName.LastName)",
                    example = "John.Doe",
                    required = true
            )
            @PathVariable String username,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TraineeTrainerListUpdateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "traineeUsername": "John.Doe",
                                        "trainerUsernames": ["Jane.Smith", "Bob.Wilson"]
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody TraineeTrainerListUpdateRequest request
    );


    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainee's trainings", description = "Returns training sessions with optional filters")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "List of trainings",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TrainingView.class))))
    @Timed(
            value = "gym.http.trainee.training.search",
            description = "Training search for Trainee endpoint",
            percentiles = {0.95, 0.99},
            histogram = true
    )
    @SecuredEndpointResponses
    ResponseEntity<List<TrainingView>> getTrainings(
            @Parameter(description = "User's unique username", example = "John.Doe")
            @PathVariable String username,

            @Parameter(description = "Filter trainings from this date (inclusive)", example = "2024-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @Parameter(description = "Filter trainings until this date (inclusive)", example = "2024-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @Parameter(description = "Filter by trainer username", example = "Jane.Smith")
            @RequestParam(required = false) String trainerName,

            @Parameter(description = "Filter by training type", example = "Fitness")
            @RequestParam(required = false) String trainingType
    );
}
