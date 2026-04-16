package dev.ilkersahin.java.spring.gym.integration.steps;

import dev.ilkersahin.java.spring.gym.integration.ScenarioContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class TrainingTypeSteps {
    @Autowired
    private ScenarioContext context;

    @When("I request the list of training types")
    public void requestTrainingTypes() {
        context.setLastResponse(
                given()
                        .when()
                        .get("/api/v1/training-types")
        );
    }

    @Then("the response should contain {int} training types")
    public void verifyTrainingTypeCount(int expected) {
        List<?> types = context.getLastResponse().jsonPath().getList("$");
        assertThat(types).hasSize(expected);
    }

    @Then("the response should contain training type {string}")
    public void verifyTrainingTypePresent(String typeName) {
        List<String> names = context.getLastResponse()
                .jsonPath()
                .getList("trainingType", String.class);

        assertThat(names).contains(typeName);
    }
}
