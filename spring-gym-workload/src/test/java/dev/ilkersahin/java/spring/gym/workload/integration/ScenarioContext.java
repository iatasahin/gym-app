package dev.ilkersahin.java.spring.gym.workload.integration;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

@Component
@ScenarioScope
public class ScenarioContext {
    @Getter @Setter
    private Response lastResponse;

    @Setter
    private String authToken;

    public String getAuthToken(){
        assertThat(authToken)
                .as("No auth token set. Did you log in first?")
                .isNotNull();
        return authToken;
    }
}
