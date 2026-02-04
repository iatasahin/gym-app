package dev.ilkersahin.java.spring.gym.dto.request;

import dev.ilkersahin.java.spring.gym.dto.auth.Credentials;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.Length;

public record TrainerPasswordChangeRequest(
        @Valid Credentials trainerCredentials,
        @Length(min = 4, max = 20, message = "Password length should be between 4 and 20 characters")
        String newPassword
) {

}
