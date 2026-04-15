package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TraineeRegistrationSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ScenarioContext context;

    @Before
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Given("the trainee database is empty")
    public void cleanDatabase() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("TRUNCATE TABLE trainings");
            stmt.execute("TRUNCATE TABLE trainees");
            stmt.execute("TRUNCATE TABLE username_counters");
            stmt.execute("TRUNCATE TABLE users");
            stmt.execute("TRUNCATE TABLE blacklisted_tokens");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    @When("I register a trainee with:")
    public void registerTrainee(DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().getFirst();

        Map<String, Object> body = new HashMap<>();
        body.put("firstName", row.get("firstName"));
        body.put("lastName", row.get("lastName"));

        if (row.containsKey("dateOfBirth") && !row.get("dateOfBirth").isEmpty()) {
            body.put("dateOfBirth", row.get("dateOfBirth"));
        }
        if (row.containsKey("address") && !row.get("address").isEmpty()) {
            body.put("address", row.get("address"));
        }

        Response response = given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/api/v1/trainees");

        context.setLastResponse(response);

        if (response.getStatusCode() == 201){
            String username = response.jsonPath().getString("username");
            String password = response.jsonPath().getString("password");
            context.storeCredentials(username, password);
        }
    }

    @Given("a registered trainee with:")
    public void aRegisteredTraineeWith(DataTable dataTable){
        registerTrainee(dataTable);
        assertThat(context.getLastResponse().getStatusCode())
                .as("Precondition failed: trainee registration should succeed")
                .isEqualTo(201);
    }

    @Then("the response status is {int}")
    public void verifyStatus(int expectedStatus){
        assertThat(context.getLastResponse().getStatusCode())
                .as("Expected HTTP %d but got %d. Body: %s",
                        expectedStatus,
                        context.getLastResponse().getStatusCode(),
                        context.getLastResponse().getBody().asString())
                .isEqualTo(expectedStatus);
    }

    @Then("the response contains username {string}")
    public void verifyUsername(String expectedUsername){
        String actualUsername = context.getLastResponse().jsonPath().getString("username");
        assertThat(actualUsername).isEqualTo(expectedUsername);
    }

    @Then("the response contains a password of length {int}")
    public void verifyPasswordLength(int expectedLength){
        String password = context.getLastResponse().jsonPath().getString("password");
        assertThat(password)
                .isNotNull()
                .hasSize(10);
    }
}
