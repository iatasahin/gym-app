package dev.ilkersahin.java.spring.gym.integrationtests.steps;

import dev.ilkersahin.java.spring.gym.integrationtests.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class GymServiceSteps {

    @Value("${services.gym.base-url:http://localhost:8080}")
    private String gymBaseUrl;

    @Autowired
    private ScenarioContext context;

    @Given("a registered trainee in the gym system:")
    public void registerTrainee(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap();

        Response response = given()
                .baseUri(gymBaseUrl)
                .contentType("application/json")
                .body(Map.of(
                        "firstName", data.get("firstName"),
                        "lastName", data.get("lastName")
                ))
                .when()
                .post("/api/v1/trainees");

        assertThat(response.getStatusCode())
                .as("Trainee registration failed: %s", response.getBody().asString())
                .isEqualTo(201);

        String username = response.jsonPath().getString("username");
        String password = response.jsonPath().getString("password");
        context.storeCredentials(username, password);
        context.store("traineeUsername", username);
    }

    @Given("a registered trainer in the gym system:")
    public void registerTrainer(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap();

        Response response = given()
                .baseUri(gymBaseUrl)
                .contentType("application/json")
                .body(Map.of(
                        "firstName", data.get("firstName"),
                        "lastName", data.get("lastName"),
                        "specialization", data.get("specialization")
                ))
                .when()
                .post("/api/v1/trainers");

        assertThat(response.getStatusCode())
                .as("Trainer registration failed: %s", response.getBody().asString())
                .isEqualTo(201);

        String username = response.jsonPath().getString("username");
        String password = response.jsonPath().getString("password");
        context.storeCredentials(username, password);
        context.store("trainerUsername", username);
    }

    @Given("I am authenticated as {string}")
    public void authenticate(String username) {
        String password = context.getPasswordFor(username);

        Response response = given()
                .baseUri(gymBaseUrl)
                .contentType("application/json")
                .body(Map.of(
                        "username", username,
                        "password", password
                ))
                .when()
                .post("/api/v1/auth/login");

        assertThat(response.getStatusCode())
                .as("Login failed for '%s': %s", username, response.getBody().asString())
                .isEqualTo(200);

        String token = response.jsonPath().getString("token");
        context.setAuthToken(token);
    }

    @When("I create a training session:")
    public void createTraining(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap();

        // Allow using "stored:traineeUsername" to reference stored values
        String traineeUsername = resolveValue(data.get("traineeUsername"));
        String trainerUsername = resolveValue(data.get("trainerUsername"));

        Response response = given()
                .baseUri(gymBaseUrl)
                .contentType("application/json")
                .header("Authorization", "Bearer " + context.getAuthToken())
                .body(Map.of(
                        "traineeUsername", traineeUsername,
                        "trainerUsername", trainerUsername,
                        "trainingName", data.get("trainingName"),
                        "trainingType", data.get("trainingType"),
                        "trainingDate", data.get("trainingDate"),
                        "durationMinutes", Integer.parseInt(data.get("durationMinutes"))
                ))
                .when()
                .post("/api/v1/trainings");

        context.setLastResponse(response);
    }

    @When("I delete the training")
    public void deleteTraining() {
        String trainingId = context.retrieve("trainingId");

        Response response = given()
                .baseUri(gymBaseUrl)
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .delete("/api/v1/trainings/" + trainingId);

        context.setLastResponse(response);
    }

    @Then("the gym service responds with status {int}")
    public void verifyGymStatus(int expectedStatus) {
        assertThat(context.getLastResponse().getStatusCode())
                .as("Expected HTTP %d but got %d. Body: %s",
                        expectedStatus,
                        context.getLastResponse().getStatusCode(),
                        context.getLastResponse().getBody().asString())
                .isEqualTo(expectedStatus);
    }

    // =================================================================
    // Helper
    // =================================================================

    private String resolveValue(String value) {
        if (value.startsWith("stored:")) {
            String key = value.substring("stored:".length());
            return context.retrieve(key);
        }
        return value;
    }
}
