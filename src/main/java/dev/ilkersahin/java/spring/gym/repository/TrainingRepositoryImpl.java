package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.dao.TrainingDao;
import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@Slf4j
public class TrainingRepositoryImpl implements TrainingDao {

    @PersistenceContext
    private EntityManager entityManager;

    // -------------------------------------------------------------------------
    // WRITE
    // -------------------------------------------------------------------------

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Training createTraining(Training training) {
        log.debug(
                "Persisting training '{}' (trainee='{}', trainer='{}', date={})",
                training.getTrainingName(),
                training.getTrainee().getUser().getUsername(),
                training.getTrainer().getUser().getUsername(),
                training.getTrainingDate()
        );
        entityManager.persist(training);
        return training;
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTraining(UUID traineeID, UUID trainerID, LocalDate trainingDate) {
        log.debug(
                "Finding trainings by IDs: traineeId={}, trainerId={}, date={}",
                traineeID, trainerID, trainingDate
        );

        if (traineeID == null || trainerID == null || trainingDate == null) {
            return List.of();
        }

        return entityManager.createQuery(
                        """
                                select t
                                from Training t
                                where t.trainee.traineeId = :traineeId
                                  and t.trainer.trainerId = :trainerId
                                  and t.trainingDate = :date
                                """,
                        Training.class
                )
                .setParameter("traineeId", traineeID)
                .setParameter("trainerId", trainerID)
                .setParameter("date", trainingDate)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getAllTrainings() {
        log.debug("Retrieving all trainings");

        return entityManager.createQuery(
                        """
                                select t
                                from Training t
                                order by t.trainingDate desc
                                """,
                        Training.class
                )
                .getResultList();
    }

    // -------------------------------------------------------------------------
    // SEARCH — TRAINEE
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Training> findForTrainee(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            TrainingType.Type trainingType
    ) {
        log.debug(
                "Searching trainings for trainee='{}', from={}, to={}, trainer={}, type={}",
                traineeUsername, fromDate, toDate, trainerUsername, trainingType
        );

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> cq = cb.createQuery(Training.class);

        Root<Training> training = cq.from(Training.class);

        Join<Object, Object> trainee = training.join("trainee");
        Join<Object, Object> traineeUser = trainee.join("user");
        Join<Object, Object> trainer = training.join("trainer");
        Join<Object, Object> trainerUser = trainer.join("user");
        Join<Object, Object> type = training.join("trainingType");

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(traineeUser.get("username"), traineeUsername));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    training.get("trainingDate"), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    training.get("trainingDate"), toDate));
        }
        if (trainerUsername != null && !trainerUsername.isBlank()) {
            predicates.add(cb.equal(
                    trainerUser.get("username"), trainerUsername));
        }
        if (trainingType != null) {
            predicates.add(cb.equal(
                    type.get("trainingTypeId"), trainingType.getId()
            ));
        }

        cq.select(training)
                .where(cb.and(predicates.toArray(Predicate[]::new)))
                .orderBy(cb.desc(training.get("trainingDate")));

        return entityManager.createQuery(cq).getResultList();
    }

    // -------------------------------------------------------------------------
    // SEARCH — TRAINER
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Training> findForTrainer(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername
    ) {
        log.debug(
                "Searching trainings for trainer='{}', from={}, to={}, traineeUsername='{}'",
                trainerUsername, fromDate, toDate, traineeUsername
        );

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> cq = cb.createQuery(Training.class);

        Root<Training> training = cq.from(Training.class);

        Join<Object, Object> trainer = training.join("trainer");
        Join<Object, Object> trainerUser = trainer.join("user");
        Join<Object, Object> trainee = training.join("trainee");
        Join<Object, Object> traineeUser = trainee.join("user");

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(trainerUser.get("username"), trainerUsername));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    training.get("trainingDate"), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    training.get("trainingDate"), toDate));
        }
        if (traineeUsername != null && !traineeUsername.isBlank()) {
            predicates.add(cb.equal(
                    traineeUser.get("username"), traineeUsername));
        }

        cq.select(training)
                .where(cb.and(predicates.toArray(Predicate[]::new)))
                .orderBy(cb.desc(training.get("trainingDate")));

        return entityManager.createQuery(cq).getResultList();
    }
}
