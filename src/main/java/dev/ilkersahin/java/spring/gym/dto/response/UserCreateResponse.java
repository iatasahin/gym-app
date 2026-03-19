package dev.ilkersahin.java.spring.gym.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after successful user registration")
public record UserCreateResponse(
        @Schema(description = "Auto-generated username (FirstName.LastName format)", example = "John.Doe")
        String username,

        @Schema(description = "Auto-generated password - **save this, it won't be shown again!**", example = "aB3dEf9xYz")
        String password
) {
}
