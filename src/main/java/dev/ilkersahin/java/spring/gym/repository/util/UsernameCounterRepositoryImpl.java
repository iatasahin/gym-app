package dev.ilkersahin.java.spring.gym.repository.util;

import dev.ilkersahin.java.spring.gym.dao.util.UsernameCounterDao;
import dev.ilkersahin.java.spring.gym.model.util.UsernameCounter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UsernameCounterRepositoryImpl implements UsernameCounterDao {
    @PersistenceContext
    @Setter(onMethod_ = {@Autowired})
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    public UsernameCounter findByBaseUsernameWithLock(String baseUsername) {
        return entityManager.find(
                UsernameCounter.class,
                baseUsername,
                LockModeType.PESSIMISTIC_WRITE
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void persist(UsernameCounter counter) {
        entityManager.persist(counter);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public UsernameCounter merge(UsernameCounter counter) {
        return entityManager.merge(counter);
    }
}
