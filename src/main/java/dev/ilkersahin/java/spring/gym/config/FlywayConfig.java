package dev.ilkersahin.java.spring.gym.config;

import lombok.Setter;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {
    @Setter(onMethod_ = @Autowired)
    private Environment env;

    @Bean(initMethod = "migrate")
    @DependsOn("dataSource")
    public Flyway flyway(DataSource dataSource) {
        boolean flywayEnabled = env.getProperty("flyway.enabled", Boolean.class, true);

        if (!flywayEnabled) {
            return null;
        }

        return Flyway.configure()
                .dataSource(dataSource)
                .locations(env.getProperty("flyway.locations", "classpath:db/migration"))
                .baselineOnMigrate(env.getProperty("flyway.baseline-on-migrate", Boolean.class, true))
                .validateOnMigrate(env.getProperty("flyway.validate-on-migrate", Boolean.class, true))
                .cleanDisabled(true) // Safety: prevent accidental data loss
                .load();
    }
}
