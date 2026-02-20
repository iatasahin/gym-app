package dev.ilkersahin.java.spring.gym.model.util;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "login_attempts",
        indexes = {
                @Index(name = "idx_login_attempts_username_time", columnList = "username, attempt_time")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class LoginAttempt {
        @Id
        @GeneratedValue
        @UuidGenerator
        @Column(name = "attempt_id", updatable = false, nullable = false)
        private UUID attemptId;

        @Column(name = "username", nullable = false)
        private String username;

        @Column(name = "attempt_time", nullable = false)
        private Instant attemptTime;

        @Column(name = "ip_address", length = 45)
        private String ipAddress;

        public LoginAttempt(String username, Instant attemptTime) {
                this.username = username;
                this.attemptTime = attemptTime;
        }

        public LoginAttempt(String username, Instant attemptTime, String ipAddress) {
                this.username = username;
                this.attemptTime = attemptTime;
                this.ipAddress = ipAddress;
        }
}
