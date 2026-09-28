CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(60) NOT NULL,
    password_hash VARCHAR(120) NOT NULL,
    display_name  VARCHAR(120),
    role          VARCHAR(20) NOT NULL,
    parent_id     BIGINT REFERENCES users (id) ON DELETE CASCADE,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT chk_users_role CHECK (role IN ('PARENT', 'CHILD'))
);

CREATE INDEX IF NOT EXISTS idx_users_parent ON users (parent_id);

CREATE TABLE IF NOT EXISTS subjects (
    id       BIGSERIAL PRIMARY KEY,
    owner_id BIGINT REFERENCES users (id) ON DELETE CASCADE,
    name     VARCHAR(120) NOT NULL,
    core     BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE subjects ADD COLUMN IF NOT EXISTS owner_id BIGINT REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE subjects DROP CONSTRAINT IF EXISTS uk_subjects_name;
CREATE UNIQUE INDEX IF NOT EXISTS uk_subjects_owner_name ON subjects (owner_id, name);
CREATE INDEX IF NOT EXISTS idx_subjects_owner ON subjects (owner_id);

CREATE TABLE IF NOT EXISTS settings (
    id                 BIGSERIAL PRIMARY KEY,
    owner_id           BIGINT REFERENCES users (id) ON DELETE CASCADE,
    five_reward        NUMERIC(10, 2) NOT NULL,
    four_reward        NUMERIC(10, 2) NOT NULL,
    three_penalty      NUMERIC(10, 2) NOT NULL,
    two_penalty        NUMERIC(10, 2) NOT NULL,
    core_coefficient   NUMERIC(10, 4) NOT NULL,
    other_coefficient  NUMERIC(10, 4) NOT NULL
);

ALTER TABLE settings ADD COLUMN IF NOT EXISTS owner_id BIGINT REFERENCES users (id) ON DELETE CASCADE;
CREATE UNIQUE INDEX IF NOT EXISTS uk_settings_owner ON settings (owner_id);

CREATE TABLE IF NOT EXISTS grades (
    id          BIGSERIAL PRIMARY KEY,
    owner_id    BIGINT REFERENCES users (id) ON DELETE CASCADE,
    user_id     BIGINT REFERENCES users (id) ON DELETE CASCADE,
    subject_id  BIGINT NOT NULL REFERENCES subjects (id),
    value       INTEGER NOT NULL,
    grade_date  DATE NOT NULL,
    CONSTRAINT chk_grades_value CHECK (value BETWEEN 2 AND 5)
);

ALTER TABLE grades ADD COLUMN IF NOT EXISTS owner_id BIGINT REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE grades ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users (id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_grades_date ON grades (grade_date);
CREATE INDEX IF NOT EXISTS idx_grades_subject ON grades (subject_id);
CREATE INDEX IF NOT EXISTS idx_grades_owner ON grades (owner_id);
CREATE INDEX IF NOT EXISTS idx_grades_user ON grades (user_id);