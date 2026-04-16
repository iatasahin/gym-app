package dev.ilkersahin.java.spring.gym.workload.integration.steps;

import dev.ilkersahin.java.spring.gym.workload.dto.ActionType;
import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadRequest;
import dev.ilkersahin.java.spring.gym.workload.integration.ScenarioContext;
import dev.ilkersahin.java.spring.gym.workload.integration.config.TestJwtTokenGenerator;
import dev.ilkersahin.java.spring.gym.workload.repository.TrainerWorkloadRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class WorkloadSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private ScenarioContext context;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerWorkloadRepository workloadRepository;

    @Autowired
    private TestJwtTokenGenerator tokenGenerator;

    private static final String WORKLOAD_QUEUE = "workload.queue";

    private String lastTrainerUsername;

    @Before
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Given("the workload database is clean")
    public void cleanDatabase() {
        workloadRepository.deleteAll();
    }

    @Given("I have a valid auth token")
    public void generateAuthToken() {
        String token = tokenGenerator.generateToken("test.user");
        context.setAuthToken(token);
    }

    @When("a workload message is sent:")
    public void sendWorkloadMessage(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap();

        WorkloadRequest request = new WorkloadRequest(
                data.get("trainerUsername"),
                data.get("trainerFirstName"),
                data.get("trainerLastName"),
                Boolean.parseBoolean(data.get("isActive")),
                LocalDate.parse(data.get("trainingDate")),
                Integer.parseInt(data.get("trainingDuration")),
                ActionType.valueOf(data.get("actionType"))
        );

        lastTrainerUsername = request.trainerUsername();

        jmsTemplate.convertAndSend(WORKLOAD_QUEUE, request);

        // Wait for async message processing
        if (request.actionType() == ActionType.ADD) {
            await().atMost(5, TimeUnit.SECONDS)
                    .until(() -> workloadRepository.findById(request.trainerUsername()).isPresent());
        } else {
            // For DELETE, just wait a bit for processing
            await().pollDelay(500, TimeUnit.MILLISECONDS)
                    .atMost(5, TimeUnit.SECONDS)
                    .until(() -> true);
        }
    }

    @When("I request the workload for {string}")
    public void requestWorkload(String username) {
        lastTrainerUsername = username;

        Response response = given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .get("/api/v1/workload/" + username);

        context.setLastResponse(response);
    }

    @When("I request the workload for {string} without authentication")
    public void requestWorkloadNoAuth(String username) {
        Response response = given()
                .when()
                .get("/api/v1/workload/" + username);

        context.setLastResponse(response);
    }

    @Then("the workload for {string} should exist")
    public void verifyWorkloadExists(String username) {
        lastTrainerUsername = username;

        // Fetch via REST and store response
        Response response = given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .get("/api/v1/workload/" + username);

        assertThat(response.getStatusCode())
                .as("Expected workload for '%s' to exist", username)
                .isEqualTo(200);

        context.setLastResponse(response);
    }

    @Then("the workload for {string} should not exist")
    public void verifyWorkloadNotExists(String username) {
        Response response = given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .get("/api/v1/workload/" + username);

        assertThat(response.getStatusCode())
                .as("Expected workload for '%s' to not exist (404)", username)
                .isEqualTo(404);
    }

    @Then("the response status is {int}")
    public void verifyStatus(int expectedStatus) {
        assertThat(context.getLastResponse().getStatusCode())
                .as("Expected HTTP %d but got %d. Body: %s",
                        expectedStatus,
                        context.getLastResponse().getStatusCode(),
                        context.getLastResponse().getBody().asString())
                .isEqualTo(expectedStatus);
    }

    @Then("the workload response should contain:")
    public void verifyWorkloadResponse(DataTable dataTable) {
        Map<String, String> expected = dataTable.asMap();
        Response response = context.getLastResponse();

        expected.forEach((field, expectedValue) -> {
            Object actual = response.jsonPath().get(field);
            if (actual instanceof Boolean) {
                assertThat(actual).isEqualTo(Boolean.parseBoolean(expectedValue));
            } else {
                assertThat(String.valueOf(actual))
                        .as("Field '%s'", field)
                        .isEqualTo(expectedValue);
            }
        });
    }

    @Then("the workload for year {int} month {int} should have duration {long}")
    public void verifyMonthlyDuration(int year, int month, long expectedDuration) {
        // Fetch fresh data
        Response response = given()
                .header("Authorization", "Bearer " + context.getAuthToken())
                .when()
                .get("/api/v1/workload/" + lastTrainerUsername);

        assertThat(response.getStatusCode()).isEqualTo(200);

        // Navigate the nested structure: years[].months[]
        List<Map<String, Object>> years = response.jsonPath().getList("years");

        Map<String, Object> yearEntry = years.stream()
                .filter(y -> ((Integer) y.get("year")) == year)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Year " + year + " not found in workload response"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> months = (List<Map<String, Object>>) yearEntry.get("months");

        Map<String, Object> monthEntry = months.stream()
                .filter(m -> ((Integer) m.get("month")) == month)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Month " + month + " not found in year " + year));

        long actualDuration = ((Number) monthEntry.get("trainingSummaryDuration")).longValue();

        assertThat(actualDuration)
                .as("Duration for %d/%d", year, month)
                .isEqualTo(expectedDuration);
    }
}
