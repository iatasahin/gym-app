package dev.ilkersahin.java.spring.gym.repository.util;

import dev.ilkersahin.java.spring.gym.model.util.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {

    /**
     * Count failed login attempts for a username within the time window.
     *
     * @param username the username to check
     * @param since    the start of the time window
     * @return number of failed attempts
     */
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.username = :username AND la.attemptTime > :since")
    int countRecentAttempts(@Param("username") String username, @Param("since") Instant since);

    /**
     * Find the most recent login attempt for a username.
     *
     * @param username the username to check
     * @return most recent attempt if exists
     */
    @Query("SELECT la FROM LoginAttempt la WHERE la.username = :username ORDER BY la.attemptTime DESC LIMIT 1")
    Optional<LoginAttempt> findMostRecentAttempt(@Param("username") String username);

    /**
     * Delete all login attempts for a username (on successful login).
     *
     * @param username the username to clear
     */
    @Modifying
    @Query("DELETE FROM LoginAttempt la WHERE la.username = :username")
    void deleteByUsername(@Param("username") String username);

    /**
     * Delete old login attempts (for cleanup job).
     *
     * @param before delete attempts older than this time
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM LoginAttempt la WHERE la.attemptTime < :before")
    int deleteOldAttempts(@Param("before") Instant before);
}
