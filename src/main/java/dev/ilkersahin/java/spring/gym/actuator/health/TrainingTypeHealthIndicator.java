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
        boolean exists = !em.createQuery("SELECT t.id FROM TrainingType t")
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
        return exists
                ? Health.up().withDetail("trainingTypes", "exist").build()
                : Health.down().withDetail("trainingTypes", 0).build();
    }
}
