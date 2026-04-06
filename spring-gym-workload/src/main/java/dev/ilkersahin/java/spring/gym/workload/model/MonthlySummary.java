package dev.ilkersahin.java.spring.gym.workload.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MonthlySummary {
    private int month;

    private long trainingSummaryDuration;

    public MonthlySummary(int month, long trainingSummaryDuration) {
        this.month = month;
        this.trainingSummaryDuration = trainingSummaryDuration;
    }
}
