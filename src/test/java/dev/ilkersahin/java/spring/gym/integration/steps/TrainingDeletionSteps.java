package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.repository.TrainingRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TrainingDeletionSteps {

    @Autowired
    private ScenarioContext context;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Given("a training exists:")
    public void aTrainingExists(DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().getFirst();

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
                .as("Precondition failed: training creation should succeed. Body: %s",
                        response.getBody().asString())
                .isEqualTo(200);

        Training training = trainingRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError("Training was not persisted"));
        context.setLastTrainingId(training.getTrainingId());

        // Drain the ADD message so it doesn't interfere
        // with DELETE message verification in the scenario
        jmsTemplate.setReceiveTimeout(1000);
        jmsTemplate.receive("workload.queue");
    }

    @When("I delete the training")
    public void deleteTraining() {
        Response response = given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .delete("/api/v1/trainings/" + context.getLastTrainingId());

        context.setLastResponse(response);
    }

    @When("I delete training with id {string}")
    public void deleteTrainingById(String trainingId) {
        Response response = given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .delete("/api/v1/trainings/" + trainingId);

        context.setLastResponse(response);
    }

    @When("I delete the training without authentication")
    public void deleteTrainingWithoutAuth() {
        Response response = given()
                .when()
                .delete("/api/v1/trainings/" + context.getLastTrainingId());

        context.setLastResponse(response);
    }
}
