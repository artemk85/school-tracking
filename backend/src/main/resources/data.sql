INSERT INTO settings (id, five_reward, four_reward, three_penalty, two_penalty, core_coefficient, other_coefficient)
VALUES (1, 75.00, 50.00, -50.00, -100.00, 1.0000, 0.7000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO subjects (name, core) VALUES
    ('Математика', TRUE),
    ('Русский язык', TRUE),
    ('Литература', FALSE),
    ('История', FALSE),
    ('Биология', FALSE),
    ('География', FALSE),
    ('Физика', FALSE),
    ('Химия', FALSE),
    ('Английский язык', FALSE),
    ('Информатика', FALSE),
    ('Окружающий мир', FALSE),
    ('Физкультура', FALSE)
ON CONFLICT (name) DO NOTHING;