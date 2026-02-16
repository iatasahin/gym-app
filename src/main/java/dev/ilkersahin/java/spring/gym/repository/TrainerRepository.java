package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainerRepository extends JpaRepository<Trainer, UUID> {

    /**
     * Find trainer by username with user eagerly fetched.
     */
    @Query("""
            SELECT t FROM Trainer t
                JOIN FETCH t.user u
            WHERE u.username = :username
            """)
    Optional<Trainer> findByUserUsername(@Param("username") String username);

    /**
     * Get all trainers with user eagerly fetched, ordered by username.
     */
    @Query("""
            SELECT t FROM Trainer t
                JOIN FETCH t.user
            ORDER BY t.user.username
            """)
    List<Trainer> findAllWithUser();

    /**
     * Find trainees assigned to a trainer.
     */
    @Query("""
            SELECT t FROM Trainer tr
                JOIN tr.trainees t
                JOIN tr.user u
            WHERE u.username = :username
            ORDER BY t.user.username
            """)
    List<Trainee> findAssignedTrainees(@Param("username") String trainerUsername);

    /**
     * Find trainers not assigned to a specific trainee.
     */
    @Query("""
            SELECT t FROM Trainer t
                JOIN t.user u
            WHERE t NOT IN (
                SELECT trr FROM Trainee tre
                    JOIN tre.trainers trr
                    JOIN tre.user tu
                WHERE tu.username = :traineeUsername
            )
            ORDER BY u.username
            """)
    List<Trainer> findTrainersNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);

    /**
     * Find trainers by list of usernames.
     * Derived query using IN clause.
     */
    @Query("""
            SELECT t FROM Trainer t
                JOIN FETCH t.user u
                JOIN FETCH t.specialization
            WHERE u.username IN :usernames
            """)
    List<Trainer> findByUserUsernames(@Param("usernames") List<String> usernames);

}
