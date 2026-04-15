package dev.ilkersahin.java.spring.gym.workload.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class YearSummary {
    private int year;

    private List<MonthlySummary> months = new ArrayList<>();

    public YearSummary(int year) {
        this.year = year;
    }
}
