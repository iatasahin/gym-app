-- Create trainees_trainers table

CREATE TABLE trainees_trainers
(
    trainee_id BINARY(16) NOT NULL,
    trainer_id BINARY(16) NOT NULL,

    CONSTRAINT pk_trainees_trainers
        PRIMARY KEY (trainee_id, trainer_id),

    CONSTRAINT fk_tt_trainee
        FOREIGN KEY (trainee_id)
            REFERENCES trainees (trainee_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_tt_trainer
        FOREIGN KEY (trainer_id)
            REFERENCES trainers (trainer_id)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_tt_trainees ON trainees_trainers (trainee_id);
CREATE INDEX idx_tt_trainers ON trainees_trainers (trainer_id);
CREATE INDEX idx_tt_trainees_trainers ON trainees_trainers (trainee_id, trainer_id);
CREATE INDEX idx_tt_trainers_trainees ON trainees_trainers (trainer_id, trainee_id);
