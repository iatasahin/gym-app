package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TrainingRepository extends JpaRepository<Training, UUID>, TrainingRepositoryCustom {

    /**
     * Find trainings by trainee ID, trainer ID, and date.
     */
    @Query("""
            SELECT t FROM Training t
            WHERE t.trainee.traineeId = :traineeId
              AND t.trainer.trainerId = :trainerId
              AND t.trainingDate = :date
            """)
    List<Training> findByTraineeAndTrainerAndDate(
            @Param("traineeId") UUID traineeId,
            @Param("trainerId") UUID trainerId,
            @Param("date") LocalDate trainingDate
    );

    /**
     * Get all trainings ordered by date descending.
     */
    List<Training> findAllByOrderByTrainingDateDesc();
}
