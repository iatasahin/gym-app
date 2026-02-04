package dev.ilkersahin.java.spring.gym.dto.view;

public record TrainerView (
        String username,
        String firstName,
        String lastName,
        boolean active,
        String specialization
){
}
