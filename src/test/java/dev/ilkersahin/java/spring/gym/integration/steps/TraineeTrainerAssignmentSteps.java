package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TraineeTrainerAssignmentSteps {

    @Autowired
    private ScenarioContext context;

    @When("I update trainers for {string} with:")
    public void updateTrainers(String traineeUsername, DataTable dataTable) {
        // Single-column DataTable → list of trainer usernames
        List<String> trainerUsernames = dataTable.asList();

        Response response = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + context.getAuthToken())
                .body(Map.of(
                        "traineeUsername", traineeUsername,
                        "trainerUsernames", trainerUsernames
                ))
                .when()
                .put("/api/v1/trainees/" + traineeUsername + "/trainers");

        context.setLastResponse(response);
    }

    @When("I update trainers for {string} with empty list")
    public void updateTrainersWithEmptyList(String traineeUsername) {
        Response response = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + context.getAuthToken())
                .body(Map.of(
                        "traineeUsername", traineeUsername,
                        "trainerUsernames", List.of()
                ))
                .when()
                .put("/api/v1/trainees/" + traineeUsername + "/trainers");

        context.setLastResponse(response);
    }

    @Then("the response should contain {int} trainer(s)")
    public void verifyTrainerCount(int expectedCount) {
        List<?> trainers = context.getLastResponse().jsonPath().getList("$");
        assertThat(trainers).hasSize(expectedCount);
    }

    @Then("the response should contain trainer {string}")
    public void verifyTrainerInResponse(String trainerUsername) {
        List<String> usernames = context.getLastResponse()
                .jsonPath()
                .getList("username", String.class);

        assertThat(usernames)
                .as("Expected trainer '%s' in response", trainerUsername)
                .contains(trainerUsername);
    }
}
