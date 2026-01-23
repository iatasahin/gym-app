-- Table to track username suffixes
CREATE TABLE username_counters
(
    base_username   VARCHAR(255) NOT NULL,
    current_suffix  INT          NOT NULL DEFAULT 2,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME,

    CONSTRAINT pk_username_counters PRIMARY KEY (base_username),

    CONSTRAINT chk_suffix_positive CHECK (current_suffix > 1)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;
