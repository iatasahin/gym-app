-- Create trainees table

CREATE TABLE trainees
(
    trainee_id    BINARY(16) NOT NULL,
    date_of_birth DATE,
    address       VARCHAR(255),
    user_id       BINARY(16) NOT NULL,

    CONSTRAINT pk_trainees PRIMARY KEY (trainee_id),

    CONSTRAINT uk_trainees_user_id
        UNIQUE (user_id),

    CONSTRAINT fk_trainees_users
        FOREIGN KEY (user_id)
            REFERENCES users (user_id)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_trainees_date_of_birth ON trainees (date_of_birth);
