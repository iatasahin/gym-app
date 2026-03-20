package dev.ilkersahin.java.spring.gym.workload.repository;

import dev.ilkersahin.java.spring.gym.workload.model.TrainerWorkload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkload, String> {
}
