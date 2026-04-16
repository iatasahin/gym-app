package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class ChangePasswordSteps {

    @Autowired
    private ScenarioContext context;


    @When("I change password for trainee {string} to {string}")
    public void changeTraineePassword(String username, String newPassword) {
        String oldPassword = context.getPasswordFor(username);

        Response response = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + context.getAuthToken())
                .body(buildPasswordChangeBody(username, oldPassword, newPassword))
                .when()
                .put("/api/v1/trainees/" + username + "/password");

        context.setLastResponse(response);
    }

    @When("I change password for trainee {string} to {string} without authentication")
    public void changeTraineePasswordNoAuth(String username, String newPassword) {
        String oldPassword = context.getPasswordFor(username);

        Response response = given()
                .contentType("application/json")
                .body(buildPasswordChangeBody(username, oldPassword, newPassword))
                .when()
                .put("/api/v1/trainees/" + username + "/password");

        context.setLastResponse(response);
    }

    @When("I change password for trainer {string} to {string}")
    public void changeTrainerPassword(String username, String newPassword) {
        String oldPassword = context.getPasswordFor(username);

        Response response = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + context.getAuthToken())
                .body(buildPasswordChangeBody(username, oldPassword, newPassword))
                .when()
                .put("/api/v1/trainers/" + username + "/password");

        context.setLastResponse(response);
    }

    @When("I change password for trainer {string} to {string} without authentication")
    public void changeTrainerPasswordNoAuth(String username, String newPassword) {
        String oldPassword = context.getPasswordFor(username);

        Response response = given()
                .contentType("application/json")
                .body(buildPasswordChangeBody(username, oldPassword, newPassword))
                .when()
                .put("/api/v1/trainers/" + username + "/password");

        context.setLastResponse(response);
    }

    @When("I log in as {string} with password {string}")
    public void loginWithPassword(String username, String password) {
        Response response = given()
                .contentType("application/json")
                .body(Map.of("username", username, "password", password))
                .when()
                .post("/api/v1/auth/login");

        context.setLastResponse(response);

        if (response.getStatusCode() == 200) {
            context.setAuthToken(response.jsonPath().getString("token"));
        }
    }

    @When("I log in as {string} with original password")
    public void loginWithOriginalPassword(String username) {
        // getPasswordFor returns the password stored at registration —
        // which is now the OLD password after a change
        String originalPassword = context.getPasswordFor(username);

        Response response = given()
                .contentType("application/json")
                .body(Map.of("username", username, "password", originalPassword))
                .when()
                .post("/api/v1/auth/login");

        context.setLastResponse(response);
    }

    // =================================================================
    // Helper
    // =================================================================

    private Map<String, String> buildPasswordChangeBody(
            String username, String oldPassword, String newPassword) {
        return Map.of(
                "username", username,
                "oldPassword", oldPassword,
                "newPassword", newPassword
        );
    }
}
