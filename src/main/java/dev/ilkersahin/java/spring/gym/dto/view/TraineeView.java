package dev.ilkersahin.java.spring.gym.dto.view;

import java.time.LocalDate;

public record TraineeView(
        String username,
        String firstName,
        String lastName,
        boolean active,
        LocalDate dateOfBirth,
        String address
) {

}
