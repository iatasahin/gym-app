package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;

public class TrainerProfileSteps {

    @Autowired
    private ScenarioContext context;

    @When("I request the trainer profile for {string}")
    public void requestTrainerProfile(String username) {
        Response response = given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .get("/api/v1/trainers/" + username);

        context.setLastResponse(response);
    }

    @When("I request the trainer profile for {string} without authentication")
    public void requestTrainerProfileNoAuth(String username) {
        Response response = given()
                .when()
                .get("/api/v1/trainers/" + username);

        context.setLastResponse(response);
    }
}
