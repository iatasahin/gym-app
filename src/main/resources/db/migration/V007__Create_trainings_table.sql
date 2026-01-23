-- Create trainings table

CREATE TABLE trainings
(
    training_id               BINARY(16)   NOT NULL,
    trainee_id                BINARY(16)   NOT NULL,
    trainer_id                BINARY(16)   NOT NULL,
    training_name             VARCHAR(100) NOT NULL,
    training_type_id          INT          NOT NULL,
    training_date             DATE         NOT NULL,
    training_duration_minutes BIGINT       NOT NULL,

    CONSTRAINT pk_trainings PRIMARY KEY (training_id),

    CONSTRAINT fk_training_trainee
        FOREIGN KEY (trainee_id)
            REFERENCES trainees (trainee_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_training_trainer
        FOREIGN KEY (trainer_id)
            REFERENCES trainers (trainer_id),

    CONSTRAINT fk_training_type
        FOREIGN KEY (training_type_id)
            REFERENCES training_types (training_type_id)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_training_date ON trainings (training_date);
CREATE INDEX idx_training_trainer ON trainings (trainer_id);
CREATE INDEX idx_training_trainee ON trainings (trainee_id);
CREATE INDEX idx_training_trainer_trainee ON trainings (trainer_id, trainee_id);
CREATE INDEX idx_training_trainee_trainer ON trainings (trainee_id, trainer_id);
CREATE INDEX idx_training_training_type ON trainings (training_type_id);
