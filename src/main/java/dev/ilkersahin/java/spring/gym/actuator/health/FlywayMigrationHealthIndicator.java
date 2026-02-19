package dev.ilkersahin.java.spring.gym.actuator.health;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlywayMigrationHealthIndicator implements HealthIndicator {

    private final Flyway flyway;

    @Override
    public Health health() {
        var info = flyway.info().current();
        if(info == null){
            return Health.down()
                    .withDetail("migration", "No migrations applied")
                    .build();
        }
        return Health.up()
                .withDetail("version", info.getVersion().toString())
                .withDetail("description", info.getDescription())
                .build();
    }
}
