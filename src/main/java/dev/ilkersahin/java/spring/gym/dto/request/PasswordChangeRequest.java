package dev.ilkersahin.java.spring.gym.dto.request;

public record PasswordChangeRequest(
        String username,
        String oldPassword,
        String newPassword
) {
}
