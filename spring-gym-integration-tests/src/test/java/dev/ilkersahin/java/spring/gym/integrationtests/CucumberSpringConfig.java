package dev.ilkersahin.java.spring.gym.integrationtests;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@CucumberContextConfiguration
@SpringBootTest(classes = SpringGymIntegrationTestsApplication.class)
@TestPropertySource("classpath:application-e2e.properties")
public class CucumberSpringConfig {
}
