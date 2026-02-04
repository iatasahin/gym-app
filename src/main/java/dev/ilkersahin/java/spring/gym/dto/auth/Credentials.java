package dev.ilkersahin.java.spring.gym.dto.auth;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record Credentials(
        @NotBlank String username,
        @NotBlank
        @Length(min = 4, max = 20, message = "Password length should be between 4 and 20 characters")
        String password
) {
}
