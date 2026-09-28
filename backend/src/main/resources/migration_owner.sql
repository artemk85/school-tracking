-- Миграция данных к схеме с авторизацией.
--
-- Создаёт родителя-владельца данных и привязывает к нему существующие
-- "бесхозные" записи (owner_id IS NULL): предметы, оценки, настройки.
--
-- Логин:  artemk
-- Пароль: school123   (BCrypt, сгенерирован BCryptPasswordEncoder из Spring Security 6.2)
--
-- Скрипт идемпотентен: повторный запуск ничего не дублирует.
-- ВАЖНО: сначала смените пароль (в UI или через /api/auth/password).

BEGIN;

-- 1. Родитель-владелец. Если пользователь artemk уже существует — берём его id.
INSERT INTO users (username, password_hash, display_name, role, created_at)
SELECT 'artemk',
       '$2a$10$i2R9tV96P6IjIMBKYCJa5uvqgB8TIuIFqfZMcoJ2uMNou.ufqzIve',
       'artemk',
       'PARENT',
       now()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE lower(username) = lower('artemk'));

-- 2. Привязка существующих данных к владельцу.
UPDATE subjects
SET owner_id = (SELECT id FROM users WHERE lower(username) = lower('artemk'))
WHERE owner_id IS NULL;

UPDATE settings
SET owner_id = (SELECT id FROM users WHERE lower(username) = lower('artemk'))
WHERE owner_id IS NULL;

-- Оценки старого формата относились к безымянному ребёнку. Чтобы данные были
-- видны, привязываем их к родителю: owner_id = родитель, user_id = тот же родитель.
UPDATE grades
SET owner_id = (SELECT id FROM users WHERE lower(username) = lower('artemk')),
    user_id  = (SELECT id FROM users WHERE lower(username) = lower('artemk'))
WHERE owner_id IS NULL OR user_id IS NULL;

COMMIT;