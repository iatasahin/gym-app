package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.client.WorkloadRequest;
import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TrainingSteps {

    @Autowired
    private ScenarioContext context;

    @Autowired
    private JmsTemplate jmsTemplate;

    @When("I create a training with:")
    public void createTraining(DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);

        Response response = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + context.getAuthToken())
                .body(buildTrainingBody(row))
                .when()
                .post("/api/v1/trainings");

        context.setLastResponse(response);
    }

    @When("I create a training without authentication:")
    public void createTrainingWithoutAuth(DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);

        Response response = given()
                .contentType("application/json")
                .body(buildTrainingBody(row))
                .when()
                .post("/api/v1/trainings");

        context.setLastResponse(response);
    }

    @Then("a message should be on the {string} queue")
    public void verifyMessageExists(String queueName) {
        jmsTemplate.setReceiveTimeout(3000);
        Object received = jmsTemplate.receiveAndConvert(queueName);

        assertThat(received)
                .as("Expected a message on queue '%s' but none arrived within 3 seconds", queueName)
                .isNotNull();

        context.setReceivedMessage(received);
    }

    @Then("the workload message should contain:")
    public void verifyWorkloadContent(DataTable dataTable) {
        WorkloadRequest workload = context.getReceivedMessage(WorkloadRequest.class);
        Map<String, String> expected = dataTable.asMap();

        expected.forEach((field, expectedValue) -> {
            String actual = extractField(workload, field);
            assertThat(actual)
                    .as("WorkloadRequest.%s", field)
                    .isEqualTo(expectedValue);
        });
    }

    // =================================================================
    // Helpers
    // =================================================================

    private Map<String, Object> buildTrainingBody(Map<String, String> row) {
        return Map.of(
                "traineeUsername", row.get("traineeUsername"),
                "trainerUsername", row.get("trainerUsername"),
                "trainingName", row.get("trainingName"),
                "trainingType", row.get("trainingType"),
                "trainingDate", row.get("trainingDate"),
                "durationMinutes", Integer.parseInt(row.get("durationMinutes"))
        );
    }

    private String extractField(WorkloadRequest workload, String field) {
        return switch (field) {
            case "trainerUsername" -> workload.trainerUsername();
            case "trainerFirstName" -> workload.trainerFirstName();
            case "trainerLastName" -> workload.trainerLastName();
            case "isActive" -> workload.isActive().toString();
            case "trainingDate" -> workload.trainingDate().toString();
            case "trainingDuration" -> String.valueOf(workload.trainingDuration());
            case "actionType" -> workload.actionType().name();
            default -> throw new IllegalArgumentException("Unknown WorkloadRequest field: " + field);
        };
    }
}
