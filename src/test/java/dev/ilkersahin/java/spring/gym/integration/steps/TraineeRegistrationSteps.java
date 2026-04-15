package dev.ilkersahin.java.spring.gym.integration.steps;

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

    private Response lastResponse;

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

        lastResponse = given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/api/v1/trainees");

    }

    @Then("the response status is {int}")
    public void verifyStatus(int expectedStatus){
        assertThat(lastResponse.getStatusCode())
                .as("Expected HTTP %d but got %d. Body: %s",
                        expectedStatus,
                        lastResponse.getStatusCode(),
                        lastResponse.getBody().asString())
                .isEqualTo(expectedStatus);
    }

    @Then("the response contains username {string}")
    public void verifyUsername(String expectedUsername){
        String actualUsername = lastResponse.jsonPath().getString("username");
        assertThat(actualUsername).isEqualTo(expectedUsername);
    }

    @Then("the response contains a password of length {int}")
    public void verifyPasswordLength(int expectedLength){
        String password = lastResponse.jsonPath().getString("password");
        assertThat(password)
                .isNotNull()
                .hasSize(10);
    }
}
