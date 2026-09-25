CREATE TABLE IF NOT EXISTS subjects (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(120) NOT NULL,
    core  BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_subjects_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS settings (
    id                 BIGINT PRIMARY KEY,
    five_reward        NUMERIC(10, 2) NOT NULL,
    four_reward        NUMERIC(10, 2) NOT NULL,
    three_penalty      NUMERIC(10, 2) NOT NULL,
    two_penalty        NUMERIC(10, 2) NOT NULL,
    core_coefficient   NUMERIC(10, 4) NOT NULL,
    other_coefficient  NUMERIC(10, 4) NOT NULL
);

CREATE TABLE IF NOT EXISTS grades (
    id          BIGSERIAL PRIMARY KEY,
    subject_id  BIGINT NOT NULL REFERENCES subjects (id),
    value       INTEGER NOT NULL,
    grade_date  DATE NOT NULL,
    CONSTRAINT chk_grades_value CHECK (value BETWEEN 2 AND 5)
);

CREATE INDEX IF NOT EXISTS idx_grades_date ON grades (grade_date);
CREATE INDEX IF NOT EXISTS idx_grades_subject ON grades (subject_id);