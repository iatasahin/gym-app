package dev.ilkersahin.java.spring.gym.model.util;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blacklisted_tokens",
        indexes = {
                @Index(name = "idx_blacklisted_tokens_token", columnList = "token_hash"),
                @Index(name = "idx_blacklisted_tokens_expiration", columnList = "expiration_time")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class BlacklistedToken {
        @Id
        @GeneratedValue
        @UuidGenerator
        @Column(name = "id", updatable = false, nullable = false)
        private UUID id;

        /**
         * SHA-256 hash of the token (storing full token would be less secure).
         */
        @Column(name = "token_hash", nullable = false, unique = true, length = 64)
        private String tokenHash;

        /**
         * When the token would naturally expire.
         * Used for cleanup - no need to keep blacklisted tokens after they expire.
         */
        @Column(name = "expiration_time", nullable = false)
        private Instant expirationTime;

        /**
         * When the token was blacklisted (for auditing).
         */
        @Column(name = "blacklisted_at", nullable = false)
        private Instant blacklistedAt;

        /**
         * Username associated with the token (for auditing/debugging).
         */
        @Column(name = "username", length = 255)
        private String username;

        public BlacklistedToken(String tokenHash, Instant expirationTime, String username) {
                this.tokenHash = tokenHash;
                this.expirationTime = expirationTime;
                this.blacklistedAt = Instant.now();
                this.username = username;
        }
}
