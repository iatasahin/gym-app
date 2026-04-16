package dev.ilkersahin.java.spring.gym.workload.integration;

import dev.ilkersahin.java.spring.gym.workload.integration.config.TestContainersConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@Import(TestContainersConfig.class)
public class CucumberSpringConfig {
}
