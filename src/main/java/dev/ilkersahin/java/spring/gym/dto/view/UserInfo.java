package dev.ilkersahin.java.spring.gym.dto.view;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Basic user information")
public record UserInfo(
        @Schema(example = "John.Doe")
        String username,

        @Schema(example = "John")
        String firstName,

        @Schema(example = "Doe")
        String lastName
) {
}
