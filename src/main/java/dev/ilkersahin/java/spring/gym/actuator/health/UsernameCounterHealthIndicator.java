package dev.ilkersahin.java.spring.gym.actuator.health;

import dev.ilkersahin.java.spring.gym.repository.util.UsernameCounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsernameCounterHealthIndicator implements HealthIndicator {

    private final UsernameCounterRepository repository;

    @Override
    public Health health() {
        try {
            long count = repository.count();
            return Health.up()
                    .withDetail("usernameCounters", count)
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
