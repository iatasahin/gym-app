package dev.ilkersahin.java.spring.gym.metrics;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class TrainingMetrics {
    private final Counter trainingCreatedCounter;
    private final Timer trainingCreationTimer;
    private final AtomicLong totalTrainingMinutes;

    public TrainingMetrics(MeterRegistry registry) {
        this.trainingCreatedCounter = Counter.builder("gym.training.created.total")
                .description("Total number of trainings created")
                .register(registry);

        this.trainingCreationTimer = Timer.builder("gym.training.creation.time")
                .description("Training creation execution time")
                .register(registry);

        this.totalTrainingMinutes = registry.gauge(
                "gym.training.duration.total.minutes",
                new AtomicLong(0)
        );
    }

    public void incrementTrainings() {
        trainingCreatedCounter.increment();
    }

    public void recordCreation(Runnable action) {
        trainingCreationTimer.record(action);
    }

    public void addTrainingDuration(int minutes) {
        totalTrainingMinutes.addAndGet(minutes);
    }
}
