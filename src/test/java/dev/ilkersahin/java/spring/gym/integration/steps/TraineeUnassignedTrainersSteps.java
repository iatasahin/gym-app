package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;

public class TraineeUnassignedTrainersSteps {

    @Autowired
    private ScenarioContext context;

    @When("I get unassigned trainers for {string}")
    public void getUnassignedTrainers(String username) {
        Response response = given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .get("/api/v1/trainees/" + username + "/trainers/unassigned");

        context.setLastResponse(response);
    }

    @When("I get unassigned trainers for {string} without authentication")
    public void getUnassignedTrainersNoAuth(String username) {
        Response response = given()
                .when()
                .get("/api/v1/trainees/" + username + "/trainers/unassigned");

        context.setLastResponse(response);
    }
}
