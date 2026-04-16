package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class TrainerTrainingsSteps {

    @Autowired
    private ScenarioContext context;

    @When("I get trainer trainings for {string}")
    public void getTrainerTrainings(String username) {
        Response response = given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .get("/api/v1/trainers/" + username + "/trainings");

        context.setLastResponse(response);
    }

    @When("I get trainer trainings for {string} with filters:")
    public void getTrainerTrainingsWithFilters(String username, DataTable dataTable) {
        Map<String, String> filters = dataTable.asMap();

        RequestSpecification spec = given()
                .header("Authorization", "Bearer " + context.getAuthToken());

        filters.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                spec.queryParam(key, value);
            }
        });

        Response response = spec
                .when()
                .get("/api/v1/trainers/" + username + "/trainings");

        context.setLastResponse(response);
    }
}
