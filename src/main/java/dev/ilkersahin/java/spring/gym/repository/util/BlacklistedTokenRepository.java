package dev.ilkersahin.java.spring.gym.repository.util;

import dev.ilkersahin.java.spring.gym.model.util.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {
    /**
     * Check if a token hash exists in the blacklist.
     *
     * @param tokenHash SHA-256 hash of the token
     * @return true if token is blacklisted
     */
    boolean existsByTokenHash(String tokenHash);

    /**
     * Delete expired blacklisted tokens (cleanup job).
     * Tokens that have naturally expired don't need to stay in the blacklist.
     *
     * @param before delete tokens that expired before this time
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.expirationTime < :before")
    int deleteExpiredTokens(@Param("before") Instant before);

    /**
     * Count blacklisted tokens (for metrics/monitoring).
     */
    @Query("SELECT COUNT(bt) FROM BlacklistedToken bt WHERE bt.expirationTime > :now")
    long countActiveBlacklistedTokens(@Param("now") Instant now);
}
