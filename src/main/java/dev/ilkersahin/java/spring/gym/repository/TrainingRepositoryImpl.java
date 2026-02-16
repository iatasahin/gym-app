package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.model.Training;
import dev.ilkersahin.java.spring.gym.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TrainingRepositoryImpl implements TrainingRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

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
