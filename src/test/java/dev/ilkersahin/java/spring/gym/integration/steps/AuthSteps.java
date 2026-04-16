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
    public void logInAs(String username) throws InterruptedException {
        String password = context.getPasswordFor(username);
        String previousToken = context.getAuthToken();

        Response response = doLogin(username, password);
        context.setLastResponse(response);

        if (response.getStatusCode() == 200) {
            String newToken = response.jsonPath().getString("token");

            // JWT tokens generated in the same second with identical claims
            // produce identical tokens. If we just blacklisted the old one,
            // the "new" token is also blacklisted. Wait for the next second.
            if (newToken.equals(previousToken)) {
                Thread.sleep(1100);
                response = doLogin(username, password);
                context.setLastResponse(response);
                newToken = response.jsonPath().getString("token");
            }
            context.setAuthToken(newToken);
        }
    }

    private static Response doLogin(String username, String password) {
        return given()
                .contentType("application/json")
                .body(Map.of(
                        "username", username,
                        "password", password
                ))
                .when()
                .post("/api/v1/auth/login");
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
