package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class TraineeUpdateSteps {

    @Autowired
    private ScenarioContext context;

    @When("I update trainee {string} with:")
    public void updateTrainee(String username, DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);

        Response response = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + context.getAuthToken())
                .body(buildUpdateBody(row))
                .when()
                .put("/api/v1/trainees/" + username);

        context.setLastResponse(response);
    }

    @When("I update trainee {string} without authentication:")
    public void updateTraineeNoAuth(String username, DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);

        Response response = given()
                .contentType("application/json")
                .body(buildUpdateBody(row))
                .when()
                .put("/api/v1/trainees/" + username);

        context.setLastResponse(response);
    }

    private Map<String, Object> buildUpdateBody(Map<String, String> row) {
        return Map.of(
                "username", row.get("username"),
                "firstName", row.get("firstName"),
                "lastName", row.get("lastName"),
                "dateOfBirth", row.get("dateOfBirth"),
                "address", row.get("address"),
                "active", Boolean.parseBoolean(row.get("active"))
        );
    }
}
