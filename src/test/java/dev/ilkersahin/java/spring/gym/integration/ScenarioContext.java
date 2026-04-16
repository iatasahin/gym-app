package dev.ilkersahin.java.spring.gym.integration;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Component
@ScenarioScope
public class ScenarioContext {
    @Getter @Setter
    private Response lastResponse;
    @Getter @Setter
    private String authToken;

    private final Map<String, String> registeredCredentials = new HashMap<>();

    @Setter
    private Object receivedMessage;

    @Setter
    private UUID lastTrainingId;

    public UUID getLastTrainingId() {
        assertThat(lastTrainingId)
                .as("No training ID stored. Did a previous step create a training?")
                .isNotNull();
        return lastTrainingId;
    }

    public <T> T getReceivedMessage(Class<T> type){
        assertThat(receivedMessage)
                .as("No message was received. Did the previous step consume one?")
                .isNotNull()
                .isInstanceOf(type);
        return type.cast(receivedMessage);
    }

    public void storeCredentials(String username, String rawPassword){
        registeredCredentials.put(username, rawPassword);
    }

    public String getPasswordFor(String username){
        String password = registeredCredentials.get(username);
        if(password == null){
            throw new IllegalStateException(
                "No stored credentials for '" + username + "'. " +
                "Did you register this trainee in a previous step?"
            );
        }
        return password;
    }
}
