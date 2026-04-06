package dev.ilkersahin.java.spring.gym.workload.repository;

import dev.ilkersahin.java.spring.gym.workload.model.TrainerWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {
}
