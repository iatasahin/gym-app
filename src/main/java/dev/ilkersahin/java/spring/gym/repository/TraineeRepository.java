package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TraineeRepository extends JpaRepository<Trainee, UUID> {

    /**
     * Find trainee by username with user eagerly fetched.
     */
    @Query("""
            SELECT t FROM Trainee t
                JOIN FETCH t.user u
            WHERE u.username = :username
            """)
    Optional<Trainee> findByUserUsername(@Param("username") String username);

    /**
     * Get all trainees with user eagerly fetched, ordered by username.
     */
    @Query("""
            SELECT t FROM Trainee t
                JOIN FETCH t.user u
            ORDER BY u.username
            """)
    List<Trainee> findAllWithUser();

    /**
     * Find trainers assigned to a trainee.
     * Returns Trainer entities with user and specialization eagerly fetched.
     */
    @Query("""
            SELECT tr FROM Trainee t
                JOIN t.trainers tr
                JOIN FETCH tr.user
                JOIN FETCH tr.specialization
                JOIN t.user u
            WHERE u.username = :username
            ORDER BY tr.user.username
            """)
    List<Trainer> findAssignedTrainers(@Param("username") String traineeUsername);
}
