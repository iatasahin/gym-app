package dev.ilkersahin.java.spring.gym.repository;

import dev.ilkersahin.java.spring.gym.dao.UserDao;
import dev.ilkersahin.java.spring.gym.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryImpl implements UserDao {
    @PersistenceContext
    @Setter(onMethod_ = {@Autowired})
    private EntityManager entityManager;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void persist(User user) {
        entityManager.persist(user);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public User merge(User user) {
        return entityManager.merge(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return entityManager.createQuery(
                        "select u from User u where u.username = :username",
                        User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);

        Root<User> user = cq.from(User.class);
        cq.select(cb.count(user))
                .where(cb.equal(user.get("username"), username));

        Long count = entityManager.createQuery(cq).getSingleResult();
        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAll() {
        return entityManager.createQuery(
                        "select u from User u order by u.username",
                        User.class)
                .getResultList();
    }
}
