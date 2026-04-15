package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TrainerRegistrationSteps {

    @Autowired
    private ScenarioContext context;

    @When("I register a trainer with:")
    public void registerTrainer(DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().getFirst();

        Map<String, Object> body = new HashMap<>();
        body.put("firstName", row.get("firstName"));
        body.put("lastName", row.get("lastName"));
        body.put("specialization", row.get("specialization"));

        Response response = given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/api/v1/trainers");

        context.setLastResponse(response);

        if (response.getStatusCode() == 201) {
            String username = response.jsonPath().getString("username");
            String password = response.jsonPath().getString("password");
            context.storeCredentials(username, password);
        }
    }

    @Given("a registered trainer with:")
    public void aRegisteredTrainerWith(DataTable dataTable) {
        registerTrainer(dataTable);
        assertThat(context.getLastResponse().getStatusCode())
                .as("Precondition failed: trainer registration should succeed")
                .isEqualTo(201);
    }
}
