package dev.ilkersahin.java.spring.gym.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gymapp.security.brute-force")
@Getter
@Setter
public class BruteForceProtectionProperties {
    /**
     * Maximum number of failed login attempts before blocking.
     */
    private int maxAttempts = 3;

    /**
     * Duration in minutes to block after max attempts exceeded.
     */
    private int blockDurationMinutes = 5;

    /**
     * Whether brute force protection is enabled.
     */
    private boolean enabled = true;
}
