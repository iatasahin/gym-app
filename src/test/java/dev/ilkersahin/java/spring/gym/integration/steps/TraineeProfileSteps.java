package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TraineeProfileSteps {
    @Autowired
    private ScenarioContext context;

    @Given("I am logged in as {string}")
    public void loginAs(String username) {
        String password = context.getPasswordFor(username);

        Response loginResponse = given()
                .contentType("application/json")
                .body(Map.of(
                        "username", username,
                        "password", password
                ))
                .when()
                .post("/api/v1/auth/login");

        assertThat(loginResponse.getStatusCode())
                .as("Login failed for '%s'. Body: %s",
                        username, loginResponse.getBody().asString())
                .isEqualTo(200);

        String token = loginResponse.jsonPath().getString("token");
        context.setAuthToken(token);
    }

    @When("I request the profile for {string}")
    public void requestProfile(String username) {
        Response response = given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .get("/api/v1/trainees/" + username);

        context.setLastResponse(response);
    }

    @When("I request the profile for {string} without authentication")
    public void requestProfileWithoutAuth(String username) {
        Response response = given()
                .when()
                .get("/api/v1/trainees/" + username);

        context.setLastResponse(response);
    }

    @Then("the profile response contains:")
    public void verifyProfileFields(DataTable dataTable){
        Map<String, String> expected = dataTable.asMap();
        Response response = context.getLastResponse();

        expected.forEach((field, expectedValue) ->
                assertThat(response.jsonPath().getString(field))
                        .as("Field '%s' should be '%s'", field, expectedValue)
                        .isEqualTo(expectedValue)
        );
    }
}
