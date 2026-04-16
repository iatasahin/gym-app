package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TraineeTrainingsSteps {
    @Autowired
    private ScenarioContext context;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Given("the following trainings exist:")
    public void createMultipleTrainings(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();

        for (Map<String, String> row : rows) {
            Response response = given()
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + context.getAuthToken())
                    .body(Map.of(
                            "traineeUsername", row.get("traineeUsername"),
                            "trainerUsername", row.get("trainerUsername"),
                            "trainingName", row.get("trainingName"),
                            "trainingType", row.get("trainingType"),
                            "trainingDate", row.get("trainingDate"),
                            "durationMinutes", Integer.parseInt(row.get("durationMinutes"))
                    ))
                    .when()
                    .post("/api/v1/trainings");

            assertThat(response.getStatusCode())
                    .as("Training setup failed for '%s'. Body: %s",
                            row.get("trainingName"), response.getBody().asString())
                    .isEqualTo(200);
        }

        // Drain all ADD messages — not relevant for query tests
        jmsTemplate.setReceiveTimeout(100);
        while (jmsTemplate.receive("workload.queue") != null) {
            // discard
        }
    }

    @When("I get trainings for {string}")
    public void getTrainingsNoFilters(String username) {
        Response response = given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .get("/api/v1/trainees/" + username + "/trainings");

        context.setLastResponse(response);
    }

    @When("I get trainings for {string} with filters:")
    public void getTrainingsWithFilters(String username, DataTable dataTable) {
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
                .get("/api/v1/trainees/" + username + "/trainings");

        context.setLastResponse(response);
    }

    @Then("the response should contain {int} training(s)")
    public void verifyTrainingCount(int expectedCount) {
        List<?> trainings = context.getLastResponse().jsonPath().getList("$");
        assertThat(trainings).hasSize(expectedCount);
    }
}
