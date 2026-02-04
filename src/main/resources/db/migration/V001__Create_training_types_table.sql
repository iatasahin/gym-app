-- Create training_types table

CREATE TABLE training_types
(
    training_type_id   INT PRIMARY KEY,
    training_type_name VARCHAR(50) NOT NULL UNIQUE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- Add index on training_type_name for faster lookups
CREATE INDEX idx_training_type_name ON training_types (training_type_name);
