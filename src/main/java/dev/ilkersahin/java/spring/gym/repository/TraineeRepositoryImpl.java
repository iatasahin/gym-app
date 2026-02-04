package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.dao.TraineeDao;
import dev.ilkersahin.java.spring.gym.model.Trainee;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeRepositoryImpl implements TraineeDao {
    private static final Logger log = LoggerFactory.getLogger(TraineeRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    // -------------------------------------------------------------------------
    // WRITE
    // -------------------------------------------------------------------------

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Trainee createTrainee(Trainee trainee) {
        log.debug("Persisting trainee with username '{}'", trainee.getUser().getUsername());
        entityManager.persist(trainee);
        return trainee;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Trainee updateTrainee(Trainee trainee) {
        log.debug("Merging trainee with username '{}'", trainee.getUser().getUsername());
        return entityManager.merge(trainee);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<Trainee> deleteTrainee(String traineeUsername) {
        log.warn("Deleting trainee with username '{}'", traineeUsername);

        Optional<Trainee> traineeOpt = getTrainee(traineeUsername);
        traineeOpt.ifPresent(entityManager::remove);
        return traineeOpt;
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> getTrainee(String username) {
        log.debug("Finding trainee by username '{}'", username);

        return entityManager.createQuery(
                        """
                                select t
                                from Trainee t
                                    join fetch t.user u
                                where u.username = :username
                                """,
                        Trainee.class
                )
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainee> getAllTrainees() {
        log.debug("Retrieving all trainees");

        return entityManager.createQuery(
                        """
                                select t
                                from Trainee t
                                    join fetch t.user u
                                order by u.username
                                """,
                        Trainee.class
                )
                .getResultList();
    }

    // -------------------------------------------------------------------------
    // RELATION QUERIES
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> findAssignedTrainers(String traineeUsername) {
        log.debug("Finding trainers assigned to trainee '{}'", traineeUsername);

        return entityManager.createQuery(
                        """
                                select tr
                                from Trainee t
                                    join t.trainers tr
                                    join t.user u
                                where u.username = :username
                                order by tr.user.username
                                """,
                        Trainer.class
                )
                .setParameter("username", traineeUsername)
                .getResultList();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void updateTrainers(String traineeUsername, List<Trainer> trainers) {
        log.warn("Updating trainers list for trainee '{}'", traineeUsername);

        Trainee trainee = getTrainee(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);

        entityManager.merge(trainee);
    }
}
