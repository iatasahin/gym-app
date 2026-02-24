# Create login attempts table

CREATE TABLE login_attempts
(
    attempt_id   BINARY(16)   NOT NULL,
    username     VARCHAR(255) NOT NULL,
    attempt_time TIMESTAMP(6) NOT NULL,
    ip_address   VARCHAR(45)  NULL,

    CONSTRAINT pk_login_attempts PRIMARY KEY (attempt_id)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Composite index for efficient queries: count attempts by username within time window
CREATE INDEX idx_login_attempts_username_time ON login_attempts (username, attempt_time);

-- Index for cleanup job: delete old attempts
CREATE INDEX idx_login_attempts_time ON login_attempts (attempt_time);
