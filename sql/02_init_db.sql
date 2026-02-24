BEGIN;
SET search_path = hr;

-- Employees (they include all enum values)
INSERT INTO Employees (employee_id, last_name, first_name, middle_name, home_address, education, hire_date, status)
VALUES
  (1, 'Иванов',   'Иван',   'Иванович',  'Amsterdam, NL, Herengracht 10',  'BACHELOR',  '2018-04-16', 'ACTIVE'),
  (2, 'Петров',   'Пётр',   'Петрович',  'Amsterdam, NL, Keizersgracht 50','MASTER',    '2016-09-01', 'ACTIVE'),
  (3, 'Сидорова', 'Анна',   'Олеговна',  'Utrecht, NL, Oudegracht 5',      'PHD',       '2014-02-10', 'ACTIVE'),
  (4, 'Ким',      'Мария',  'Сергеевна', 'Rotterdam, NL, Coolsingel 1',    'VOCATIONAL','2020-11-20', 'ACTIVE'),
  (5, 'Смирнов',  'Алексей','Игоревич',  'Haarlem, NL, Grote Markt 3',     'SECONDARY', '2023-06-05', 'INACTIVE');

-- Departments (create root first, then children)
INSERT INTO Departments (department_id, name, parent_department_id, manager_employee_id)
VALUES
  (1, 'Головной офис',    NULL, 2),
  (2, 'IT департамент',   1,    3),
  (3, 'HR департамент',   1,    1),
  (4, 'Финансовый отдел', 1,    2),
  (5, 'Разработка',       2,    3);

-- Positions
INSERT INTO Positions (position_id, name, responsibilities)
VALUES
  (1, 'Генеральный директор', 'Стратегическое управление компанией, принятие ключевых решений.'),
  (2, 'HR-менеджер',          'Ведение кадровых данных, подбор, оформление, процессы HR.'),
  (3, 'Backend-разработчик',  'Разработка серверной части, API, работа с БД.'),
  (4, 'QA-инженер',           'Тестирование, контроль качества, тестовая документация.'),
  (5, 'Финансовый аналитик',  'Бюджетирование, отчётность, финансовый анализ.');

INSERT INTO DepartmentPositions (dept_pos_id, department_id, position_id, slots_total)
VALUES
  (1, 1, 1, 1), -- Head Office: CEO 1
  (2, 3, 2, 2), -- HR: HR-managers 2
  (3, 5, 3, 4), -- Development: Backend dev 4
  (4, 5, 4, 2), -- Development: QA 2
  (5, 4, 5, 2), -- Finance: analysts 2
  (6, 2, 3, 1), -- IT dept: backend dev 1 (например архитектор/лид)
  (7, 2, 4, 1); -- IT dept: QA 1

-- Assignments (history + current; ensure only one active per employee)
-- Employee 2: CEO in Head Office (active)
INSERT INTO Assignments (assignment_id, employee_id, department_id, position_id, start_date, end_date, note)
VALUES
  (1, 2, 1, 1, '2019-01-01', NULL, 'Назначение на должность Генерального директора');

-- Employee 1: history: HR in HR dept (active)
INSERT INTO Assignments (assignment_id, employee_id, department_id, position_id, start_date, end_date, note)
VALUES
  (2, 1, 3, 2, '2018-04-16', NULL, 'Приём в HR департамент');

-- Employee 3: history: backend -> manager of IT, but keep position as backend for simplicity
INSERT INTO Assignments (assignment_id, employee_id, department_id, position_id, start_date, end_date, note)
VALUES
  (3, 3, 5, 3, '2014-02-10', '2017-12-31', 'Работа в разработке'),
  (4, 3, 2, 3, '2018-01-01', NULL,         'Перевод в IT департамент (текущее назначение)');

-- Employee 4: QA in Development (active), plus earlier backend short period
INSERT INTO Assignments (assignment_id, employee_id, department_id, position_id, start_date, end_date, note)
VALUES
  (5, 4, 5, 3, '2020-11-20', '2021-05-31', 'Стажировка как разработчик'),
  (6, 4, 5, 4, '2021-06-01', NULL,         'Перевод на QA');

-- Employee 5: inactive employee with past assignments (no active assignment)
INSERT INTO Assignments (assignment_id, employee_id, department_id, position_id, start_date, end_date, note)
VALUES
  (7, 5, 4, 5, '2023-06-05', '2024-03-31', 'Приём в финансовый отдел'),
  (8, 5, 4, 5, '2024-04-01', '2024-12-31', 'Продление договора');

-- Add more history rows for richer dataset (still respecting "one active per employee")
INSERT INTO Assignments (assignment_id, employee_id, department_id, position_id, start_date, end_date, note)
VALUES
  (9, 1, 3, 2, '2019-01-01',  '2020-12-31', 'Расширение обязанностей (историческая запись)'),
  (10, 1, 3, 2, '2021-01-01', '2022-12-31', 'Старший HR (история)');

COMMIT;

