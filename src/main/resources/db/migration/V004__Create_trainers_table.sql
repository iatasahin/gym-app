-- Create trainers table

CREATE TABLE trainers
(
    trainer_id        BINARY(16) NOT NULL,
    specialization_id INT        NOT NULL,
    user_id           BINARY(16) NOT NULL,

    CONSTRAINT pk_trainers PRIMARY KEY (trainer_id),

    CONSTRAINT uk_trainers_user_id
        UNIQUE (user_id),

    CONSTRAINT fk_trainers_users
        FOREIGN KEY (user_id)
            REFERENCES users (user_id),

    CONSTRAINT fk_trainers_training_types
        FOREIGN KEY (specialization_id)
            REFERENCES training_types (training_type_id)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_trainers_specialization ON trainers (specialization_id);
