package dev.ilkersahin.java.spring.gym.integrationtests;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope(proxyMode = ScopedProxyMode.NO)
@Getter
@Setter
public class ScenarioContext {

    private Response lastResponse;
    private String authToken;

    private final Map<String, String> credentials = new HashMap<>();

    private final Map<String, String> data = new HashMap<>();

    public void storeCredentials(String username, String password) {
        credentials.put(username, password);
    }

    public String getPasswordFor(String username) {
        return credentials.get(username);
    }

    public void store(String key, String value) {
        data.put(key, value);
    }

    public String retrieve(String key) {
        return data.get(key);
    }
}