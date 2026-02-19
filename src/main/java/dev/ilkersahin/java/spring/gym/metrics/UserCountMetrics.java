package dev.ilkersahin.java.spring.gym.metrics;

import dev.ilkersahin.java.spring.gym.repository.TraineeRepository;
import dev.ilkersahin.java.spring.gym.repository.TrainerRepository;
import dev.ilkersahin.java.spring.gym.repository.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class UserCountMetrics {

    public UserCountMetrics(
            MeterRegistry registry,
            UserRepository userRepo,
            TraineeRepository traineeRepo,
            TrainerRepository trainerRepo
    ) {
        Gauge.builder("gym.users.total", userRepo::count).register(registry);
        Gauge.builder("gym.trainees.total", traineeRepo::count).register(registry);
        Gauge.builder("gym.trainers.total", trainerRepo::count).register(registry);
    }
}
