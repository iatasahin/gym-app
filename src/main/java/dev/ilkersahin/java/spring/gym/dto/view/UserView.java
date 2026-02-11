package dev.ilkersahin.java.spring.gym.dto.view;

public record UserView(
        String username,
        String firstName,
        String lastName,
        boolean active
) {
}
