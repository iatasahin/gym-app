package dev.ilkersahin.java.spring.gym.workload.controller;

import dev.ilkersahin.java.spring.gym.workload.dto.WorkloadResponse;
import dev.ilkersahin.java.spring.gym.workload.exception.TrainerNotFoundException;
import dev.ilkersahin.java.spring.gym.workload.security.JwtAuthenticationFilter;
import dev.ilkersahin.java.spring.gym.workload.security.JwtService;
import dev.ilkersahin.java.spring.gym.workload.security.TransactionIdFilter;
import dev.ilkersahin.java.spring.gym.workload.security.config.SecurityConfig;
import dev.ilkersahin.java.spring.gym.workload.service.WorkloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkloadController.class)
@Import({SecurityConfig.class, TransactionIdFilter.class, JwtAuthenticationFilter.class})
class WorkloadControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkloadService workloadService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    void getWorkload_found_returns200WithBody() throws Exception {
        var response = new WorkloadResponse(
                "john.doe", "John", "Doe", true,
                List.of(new WorkloadResponse.YearSummary(2025,
                        List.of(new WorkloadResponse.MonthSummary(3, 120))))
        );
        when(workloadService.getTrainerWorkload("john.doe")).thenReturn(response);

        mockMvc.perform(get("/api/v1/workload/john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("john.doe"))
                .andExpect(jsonPath("$.trainerFirstName").value("John"))
                .andExpect(jsonPath("$.trainerLastName").value("Doe"))
                .andExpect(jsonPath("$.trainerStatus").value(true))
                .andExpect(jsonPath("$.years[0].year").value(2025))
                .andExpect(jsonPath("$.years[0].months[0].month").value(3))
                .andExpect(jsonPath("$.years[0].months[0].trainingSummaryDuration").value(120));
    }

    @Test
    @WithMockUser
    void getWorkload_notFound_returns404() throws Exception {
        when(workloadService.getTrainerWorkload("unknown"))
                .thenThrow(new TrainerNotFoundException("Trainer workload not found for username: unknown"));

        mockMvc.perform(get("/api/v1/workload/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Trainer workload not found for username: unknown"));
    }

    @Test
    void getWorkload_unauthenticated_denied() throws Exception {
        mockMvc.perform(get("/api/v1/workload/john.doe"))
                .andExpect(status().isForbidden());
    }
}