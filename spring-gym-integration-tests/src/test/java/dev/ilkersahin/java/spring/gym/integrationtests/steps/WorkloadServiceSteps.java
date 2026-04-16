package dev.ilkersahin.java.spring.gym.integrationtests.steps;

import dev.ilkersahin.java.spring.gym.integrationtests.ScenarioContext;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class WorkloadServiceSteps {
    @Value("${services.workload.base-url:http://localhost:8082}")
    private String workloadBaseUrl;

    @Value("${e2e.async.timeout-seconds:10}")
    private int asyncTimeoutSeconds;

    @Autowired
    private ScenarioContext context;

    // =================================================================
    // THEN — Workload verification (with async wait)
    // =================================================================

    @Then("the trainer {string} workload should show {int} minutes for {int}-{int}")
    public void verifyWorkloadDuration(String trainerUsername, int expectedMinutes, int year, int month) {
        // Resolve stored values
        String username = trainerUsername.startsWith("stored:")
                ? context.retrieve(trainerUsername.substring("stored:".length()))
                : trainerUsername;

        // Wait for async message processing and verify
        await().atMost(asyncTimeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Response response = given()
                            .baseUri(workloadBaseUrl)
                            .header("Authorization", "Bearer " + context.getAuthToken())
                            .when()
                            .get("/api/v1/workload/" + username);

                    assertThat(response.getStatusCode())
                            .as("Workload not found for '%s'", username)
                            .isEqualTo(200);

                    long actualDuration = extractDuration(response, year, month);
                    assertThat(actualDuration)
                            .as("Duration for %s in %d/%d", username, year, month)
                            .isEqualTo(expectedMinutes);
                });
    }

    @Then("the trainer {string} should have workload data")
    public void verifyWorkloadExists(String trainerUsername) {
        String username = trainerUsername.startsWith("stored:")
                ? context.retrieve(trainerUsername.substring("stored:".length()))
                : trainerUsername;

        await().atMost(asyncTimeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Response response = given()
                            .baseUri(workloadBaseUrl)
                            .header("Authorization", "Bearer " + context.getAuthToken())
                            .when()
                            .get("/api/v1/workload/" + username);

                    assertThat(response.getStatusCode())
                            .as("Expected workload for '%s' to exist", username)
                            .isEqualTo(200);
                });
    }

    @Then("the trainer {string} should have no workload data")
    public void verifyWorkloadNotExists(String trainerUsername) {
        String username = trainerUsername.startsWith("stored:")
                ? context.retrieve(trainerUsername.substring("stored:".length()))
                : trainerUsername;

        // Wait a bit for any potential message processing
        await().pollDelay(2, TimeUnit.SECONDS)
                .atMost(asyncTimeoutSeconds, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Response response = given()
                            .baseUri(workloadBaseUrl)
                            .header("Authorization", "Bearer " + context.getAuthToken())
                            .when()
                            .get("/api/v1/workload/" + username);

                    assertThat(response.getStatusCode())
                            .as("Expected workload for '%s' to NOT exist (404)", username)
                            .isEqualTo(404);
                });
    }

    // =================================================================
    // Helper
    // =================================================================

    private long extractDuration(Response response, int year, int month) {
        List<Map<String, Object>> years = response.jsonPath().getList("years");

        Map<String, Object> yearEntry = years.stream()
                .filter(y -> ((Integer) y.get("year")) == year)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Year " + year + " not found"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> months = (List<Map<String, Object>>) yearEntry.get("months");

        Map<String, Object> monthEntry = months.stream()
                .filter(m -> ((Integer) m.get("month")) == month)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Month " + month + " not found"));

        return ((Number) monthEntry.get("trainingSummaryDuration")).longValue();
    }
}
