package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.dao.TrainerDao;
import dev.ilkersahin.java.spring.gym.model.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerRepositoryImpl implements TrainerDao {
    private static final Logger log = LoggerFactory.getLogger(TrainerRepositoryImpl.class);

    @PersistenceContext
    @Setter(onMethod_ = {@Autowired})
    private EntityManager entityManager;

    // -------------------------------------------------------------------------
    // WRITE
    // -------------------------------------------------------------------------

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Trainer createTrainer(Trainer trainer) {
        log.debug("Persisting trainer with username '{}'", trainer.getUser().getUsername());
        entityManager.persist(trainer);
        return trainer;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Trainer updateTrainer(Trainer trainer) {
        log.debug("Merging trainer with username '{}'", trainer.getUser().getUsername());
        return entityManager.merge(trainer);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> getTrainer(String username) {
        log.debug("Finding trainer by username '{}'", username);

        return entityManager.createQuery(
                        """
                                select t
                                from Trainer t
                                    join fetch t.user u
                                where u.username = :username
                                """,
                        Trainer.class
                )
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getAllTrainers() {
        log.debug("Retrieving all trainers");

        return entityManager.createQuery(
                        """
                                select t
                                from Trainer t
                                    join fetch t.user
                                order by t.user.username
                                """,
                        Trainer.class
                )
                .getResultList();
    }

    // -------------------------------------------------------------------------
    // RELATION QUERIES
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> findTrainersNotAssignedToTrainee(String traineeUsername) {
        log.debug("Finding trainers not assigned to trainee '{}'", traineeUsername);

        return entityManager.createQuery(
                        """
                                select t
                                from Trainer t
                                    join t.user u
                                where t not in (
                                    select trr
                                    from Trainee tre
                                        join tre.trainers trr
                                        join tre.user tu
                                    where tu.username = :traineeUsername
                                )
                                order by u.username
                                """,
                        Trainer.class
                )
                .setParameter("traineeUsername", traineeUsername)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> findByUsernames(List<String> usernames) {
        log.debug("Finding trainers by usernames {}", usernames);

        if (usernames == null || usernames.isEmpty()) {
            return List.of();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Trainer> cq = cb.createQuery(Trainer.class);

        Root<Trainer> trainer = cq.from(Trainer.class);
        cq.select(trainer)
                .where(trainer.get("user").get("username").in(usernames));

        return entityManager.createQuery(cq).getResultList();
    }
}
