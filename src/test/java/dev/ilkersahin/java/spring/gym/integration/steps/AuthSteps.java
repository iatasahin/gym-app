package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class AuthSteps {

    @Autowired
    private ScenarioContext context;

    @When("I log in as {string}")
    public void logInAs(String username) {
        String password = context.getPasswordFor(username);

        Response response = given()
                .contentType("application/json")
                .body(Map.of(
                        "username", username,
                        "password", password
                ))
                .when()
                .post("/api/v1/auth/login");

        context.setLastResponse(response);

        if (response.getStatusCode() == 200) {
            String token = response.jsonPath().getString("token");
            context.setAuthToken(token);
        }
    }

    @When("I logout")
    public void logout() {
        Response response = given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .post("/api/v1/auth/logout");

        context.setLastResponse(response);
        // Intentionally NOT clearing authToken from context.
        // The blacklisted token stays so subsequent steps
        // prove it gets rejected.
    }

    @When("I logout without authentication")
    public void logoutWithoutAuth() {
        Response response = given()
                .when()
                .post("/api/v1/auth/logout");

        context.setLastResponse(response);
    }
}
