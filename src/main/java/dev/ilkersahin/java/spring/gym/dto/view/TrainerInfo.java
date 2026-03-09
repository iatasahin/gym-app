package dev.ilkersahin.java.spring.gym.dto.view;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trainer info summary")
public record TrainerInfo(
        @Schema(example = "Jane.Smith")
        String username,

        @Schema(example = "Jane")
        String firstName,

        @Schema(example = "Smith")
        String lastName,

        @Schema(example = "Fitness")
        String specialization
) {
}
