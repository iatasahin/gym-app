package dev.ilkersahin.java.spring.gym.api;

import dev.ilkersahin.java.spring.gym.dto.view.TrainingTypeView;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Tag(name = "Training Types", description = "Training type catalog")
@Timed(
        value = "gym.http.training-type",
        description = "Training Type controller HTTP requests"
)
public interface TrainingTypeApi {
    @GetMapping
    @Operation(
            summary = "Get all training types",
            description = """
                    Returns all available training types.
                    
                    **Public endpoint - no authentication required.**
                    
                    Training types are predefined and cannot be modified by users.
                    Use these values when creating trainers (specialization) or training sessions.
                    """,
            security = {}
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of training types",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TrainingTypeView.class)),
                    examples = @ExampleObject(value = """
                            [
                                {"trainingTypeId": 1, "trainingType": "Fitness"},
                                {"trainingTypeId": 2, "trainingType": "Yoga"},
                                {"trainingTypeId": 3, "trainingType": "Zumba"},
                                {"trainingTypeId": 4, "trainingType": "Stretching"},
                                {"trainingTypeId": 5, "trainingType": "Resistance"}
                            ]
                            """)
            )
    )
    ResponseEntity<List<TrainingTypeView>> getTrainingTypes();
}
