package dev.ilkersahin.java.spring.gym.workload.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "monthly_summary",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_trainer_year_month",
                columnNames = {"trainer_username", "training_year", "training_month"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class MonthlySummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_username", nullable = false)
    private TrainerWorkload trainerWorkload;

    @Column(name = "training_year", nullable = false)
    private int year;

    @Column(name = "training_month", nullable = false)
    private int month;

    @Column(name = "training_summary_duration", nullable = false)
    private long trainingSummaryDuration;

    public MonthlySummary(TrainerWorkload trainerWorkload, int year, int month, long trainingSummaryDuration) {
        this.trainerWorkload = trainerWorkload;
        this.year = year;
        this.month = month;
        this.trainingSummaryDuration = trainingSummaryDuration;
    }
}
