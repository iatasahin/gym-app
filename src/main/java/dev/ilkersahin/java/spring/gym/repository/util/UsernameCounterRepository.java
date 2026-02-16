package dev.ilkersahin.java.spring.gym.repository.util;

import dev.ilkersahin.java.spring.gym.model.util.UsernameCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsernameCounterRepository extends JpaRepository<UsernameCounter, String> {

    /**
     * Find by base username with pessimistic write lock.
     * Equivalent to: entityManager.find(UsernameCounter.class, baseUsername, LockModeType.PESSIMISTIC_WRITE)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT uc FROM UsernameCounter uc WHERE uc.baseUsername = :baseUsername")
    Optional<UsernameCounter> findByBaseUsernameWithLock(@Param("baseUsername") String baseUsername);
}
