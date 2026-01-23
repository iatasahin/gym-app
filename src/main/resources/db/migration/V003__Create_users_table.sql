-- Create users table

CREATE TABLE users
(
    user_id    BINARY(16) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    username   VARCHAR(255)  NOT NULL,
    password   VARCHAR(255) NOT NULL,
    is_active  BOOLEAN NOT NULL DEFAULT TRUE,
    constraint pk_users PRIMARY KEY (user_id),
    constraint uk_users_username UNIQUE (username)
)
    ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_bin;

-- Index for common queries
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_last_name ON users (last_name);
CREATE INDEX idx_users_first_name ON users (first_name);
CREATE INDEX idx_users_last_name_first_name ON users (last_name, first_name);
CREATE INDEX idx_users_active ON users (is_active);
