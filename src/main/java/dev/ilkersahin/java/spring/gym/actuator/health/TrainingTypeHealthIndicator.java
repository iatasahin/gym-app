package dev.ilkersahin.java.spring.gym.actuator.health;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainingTypeHealthIndicator implements HealthIndicator {

    private final EntityManager em;

    @Override
    public Health health() {
        Long count = em.createQuery(
                "SELECT COUNT(t) FROM TrainingType t", Long.class
        ).getSingleResult();

        return count > 0
                ? Health.up().withDetail("trainingTypes", count).build()
                : Health.down().withDetail("trainingTypes", 0).build();
    }
}
