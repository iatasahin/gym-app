package dev.ilkersahin.java.spring.gym.config;

import dev.ilkersahin.java.spring.gym.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "401",
        description = """
                Missing or invalid JWT token.
                
                **Possible causes:**
                - No Authorization header provided
                - Token doesn't start with 'Bearer '
                - Token is expired
                - Token signature is invalid
                """,
        content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                        {
                            "timestamp": "2024-01-15T10:30:00",
                            "status": 401,
                            "error": "Unauthorized",
                            "message": "Missing or invalid Authorization header",
                            "path": "/api/v1/trainees/John.Doe"
                        }
                        """)
        )
)
@ApiResponse(
        responseCode = "403",
        description = "Access denied - user cannot access this resource",
        content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                        {
                            "timestamp": "2024-01-15T10:30:00",
                            "status": 403,
                            "error": "Forbidden",
                            "message": "User 'John.Doe' cannot access resources of 'Jane.Smith'",
                            "path": "/api/v1/trainees/Jane.Smith"
                        }
                        """)
        )
)
public @interface SecuredEndpointResponses {
}
