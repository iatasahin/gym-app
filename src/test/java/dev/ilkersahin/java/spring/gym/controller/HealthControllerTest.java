package dev.ilkersahin.java.spring.gym.controller;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthControllerTest {
    private final HealthController controller = new HealthController();

    @Test
    void health_shouldReturnStatusUp() {
        Map<String, Object> result = controller.health();

        assertThat(result).containsKey("status");
        assertThat(result.get("status")).isEqualTo("UP");
    }

    @Test
    void health_shouldReturnTimestamp() {
        Map<String, Object> result = controller.health();

        assertThat(result).containsKey("timestamp");
        assertThat(result.get("timestamp")).isNotNull();
    }

}
