package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthLoginSteps {

    @Autowired
    private ScenarioContext context;

    @When("I log in with empty credentials")
    public void loginWithEmptyCredentials() {
        Response response = given()
                .contentType("application/json")
                .body(Map.of("username", "", "password", ""))
                .when()
                .post("/api/v1/auth/login");

        context.setLastResponse(response);
    }

    @When("I fail to log in as {string} {int} times")
    public void failLoginMultipleTimes(String username, int times) {
        for (int i = 0; i < times; i++) {
            given()
                    .contentType("application/json")
                    .body(Map.of("username", username, "password", "WrongPassword" + i))
                    .when()
                    .post("/api/v1/auth/login");
        }
    }

    @When("I request the profile for {string} with token {string}")
    public void requestProfileWithSpecificToken(String username, String token) {
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/trainees/" + username);

        context.setLastResponse(response);
    }

    @Then("the response body contains {string}")
    public void responseBodyContains(String expectedText) {
        String body = context.getLastResponse().getBody().asString();
        assertThat(body.toLowerCase())
                .as("Expected response body to contain '%s'", expectedText)
                .contains(expectedText.toLowerCase());
    }
}
