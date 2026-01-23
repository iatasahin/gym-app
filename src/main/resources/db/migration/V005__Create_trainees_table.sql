-- Create trainees table

CREATE TABLE trainees
(
    trainee_id    BINARY(16) NOT NULL,
    date_of_birth DATE,
    address       VARCHAR(255),

    CONSTRAINT pk_trainees PRIMARY KEY (trainee_id),

    CONSTRAINT fk_trainees_user
        FOREIGN KEY (trainee_id)
            REFERENCES users (user_id)
            ON DELETE CASCADE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_trainees_date_of_birth ON trainees (date_of_birth);
