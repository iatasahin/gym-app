package dev.ilkersahin.java.spring.gym.workload.dto;

import java.util.List;

public record WorkloadResponse(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        boolean trainerStatus,
        List<YearSummary> years
) {
    public record YearSummary(
            int year,
            List<MonthSummary> months
    ) {
    }

    public record MonthSummary(
            int month,
            long trainingSummaryDuration
    ) {
    }
}
