package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;

public class TraineeDeletionSteps {

    @Autowired
    private ScenarioContext context;

    @When("I delete trainee {string}")
    public void deleteTrainee(String username) {
        Response response = given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .delete("/api/v1/trainees/" + username);

        context.setLastResponse(response);
    }

    @When("I delete trainee {string} without authentication")
    public void deleteTraineeNoAuth(String username) {
        Response response = given()
                .when()
                .delete("/api/v1/trainees/" + username);

        context.setLastResponse(response);
    }
}
