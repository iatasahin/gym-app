package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class TraineeActivationSteps {

    @Autowired
    private ScenarioContext context;

    @When("I set activation status for {string} to active")
    public void activate(String username) {
        context.setLastResponse(sendActivationRequest(username, true));
    }

    @When("I set activation status for {string} to inactive")
    public void deactivate(String username) {
        context.setLastResponse(sendActivationRequest(username, false));
    }

    @When("I set activation status for {string} to inactive without authentication")
    public void deactivateWithoutAuth(String username) {
        Response response = given()
                .contentType("application/json")
                .body(Map.of("username", username, "active", false))
                .when()
                .patch("/api/v1/trainees/" + username + "/status");

        context.setLastResponse(response);
    }

    private Response sendActivationRequest(String username, boolean active) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + context.getAuthToken())
                .body(Map.of("username", username, "active", active))
                .when()
                .patch("/api/v1/trainees/" + username + "/status");
    }
}
