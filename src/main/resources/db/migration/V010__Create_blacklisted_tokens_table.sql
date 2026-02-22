# Create blacklisted tokens table for logout/token invalidation

CREATE TABLE blacklisted_tokens
(
    id              BINARY(16)   NOT NULL,
    token_hash      VARCHAR(64)  NOT NULL,
    expiration_time TIMESTAMP(6) NOT NULL,
    blacklisted_at  TIMESTAMP(6) NOT NULL,
    username        VARCHAR(255) NULL,

    CONSTRAINT pk_blacklisted_tokens PRIMARY KEY (id),
    CONSTRAINT uk_blacklisted_tokens_hash UNIQUE (token_hash)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Index for fast token lookup during authentication
CREATE INDEX idx_blacklisted_tokens_token ON blacklisted_tokens (token_hash);

-- Index for cleanup job - find expired tokens
CREATE INDEX idx_blacklisted_tokens_expiration ON blacklisted_tokens (expiration_time);
