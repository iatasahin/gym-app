package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class TrainerUpdateSteps {

    @Autowired
    private ScenarioContext context;

    @When("I update trainer {string} with:")
    public void updateTrainer(String username, DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);

        Response response = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + context.getAuthToken())
                .body(buildUpdateBody(row))
                .when()
                .put("/api/v1/trainers/" + username);

        context.setLastResponse(response);
    }

    @When("I update trainer {string} without authentication:")
    public void updateTrainerNoAuth(String username, DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);

        Response response = given()
                .contentType("application/json")
                .body(buildUpdateBody(row))
                .when()
                .put("/api/v1/trainers/" + username);

        context.setLastResponse(response);
    }

    private Map<String, Object> buildUpdateBody(Map<String, String> row) {
        return Map.of(
                "username", row.get("username"),
                "firstName", row.get("firstName"),
                "lastName", row.get("lastName"),
                "specialization", row.get("specialization"),
                "active", Boolean.parseBoolean(row.get("active"))
        );
    }
}
