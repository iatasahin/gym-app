package dev.ilkersahin.java.spring.gym.dto.view;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Trainer profile with assigned trainees")
public record TrainerWithListView(
        @Schema(example = "Jane.Smith")
        String username,

        @Schema(example = "Jane")
        String firstName,

        @Schema(example = "Smith")
        String lastName,

        @Schema(example = "true")
        boolean active,

        @Schema(example = "Fitness")
        String specialization,

        @Schema(description = "List of assigned trainees")
        List<UserInfo> trainees
){
}
