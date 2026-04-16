package dev.ilkersahin.java.spring.gym.integration;

import dev.ilkersahin.java.spring.gym.config.TestcontainersConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@Import(TestcontainersConfig.class)
public class CucumberSpringConfig {
}
