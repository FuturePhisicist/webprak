BEGIN;
SET search_path = hr;

INSERT INTO employees (employee_id, last_name, first_name, middle_name, home_address, education, hire_date, status)
VALUES
  (1, 'Иванов',   'Иван',   'Иванович',  'Amsterdam, NL, Herengracht 10',  'BACHELOR',  '2018-04-16', 'ACTIVE'),
  (2, 'Петров',   'Пётр',   'Петрович',  'Amsterdam, NL, Keizersgracht 50','MASTER',    '2016-09-01', 'ACTIVE'),
  (3, 'Сидорова', 'Анна',   'Олеговна',  'Utrecht, NL, Oudegracht 5',      'PHD',       '2014-02-10', 'ACTIVE'),
  (4, 'Ким',      'Мария',  'Сергеевна', 'Rotterdam, NL, Coolsingel 1',    'VOCATIONAL','2020-11-20', 'ACTIVE'),
  (5, 'Смирнов',  'Алексей','Игоревич',  'Haarlem, NL, Grote Markt 3',     'SECONDARY', '2023-06-05', 'INACTIVE');

INSERT INTO departments (department_id, name, parent_department_id, manager_employee_id)
VALUES
  (1, 'Головной офис',    NULL, 2),
  (2, 'IT департамент',   1,    3),
  (3, 'HR департамент',   1,    1),
  (4, 'Финансовый отдел', 1,    2),
  (5, 'Разработка',       2,    3);

INSERT INTO positions (position_id, name, responsibilities)
VALUES
  (1, 'Генеральный директор', 'Стратегическое управление компанией, принятие ключевых решений.'),
  (2, 'HR-менеджер',          'Ведение кадровых данных, подбор, оформление, процессы HR.'),
  (3, 'Backend-разработчик',  'Разработка серверной части, API, работа с БД.'),
  (4, 'QA-инженер',           'Тестирование, контроль качества, тестовая документация.'),
  (5, 'Финансовый аналитик',  'Бюджетирование, отчётность, финансовый анализ.');

INSERT INTO department_positions (dept_pos_id, department_id, position_id, slots_total)
VALUES
  (1, 1, 1, 1),
  (2, 3, 2, 2),
  (3, 5, 3, 4),
  (4, 5, 4, 2),
  (5, 4, 5, 2),
  (6, 2, 3, 1),
  (7, 2, 4, 1);

INSERT INTO assignments (assignment_id, employee_id, department_id, position_id, start_date, end_date, note)
VALUES
  (1, 2, 1, 1, '2019-01-01', NULL, 'Назначение на должность Генерального директора'),
  (2, 1, 3, 2, '2018-04-16', NULL, 'Приём в HR департамент'),
  (3, 3, 5, 3, '2014-02-10', '2017-12-31', 'Работа в разработке'),
  (4, 3, 2, 3, '2018-01-01', NULL, 'Перевод в IT департамент (текущее назначение)'),
  (5, 4, 5, 3, '2020-11-20', '2021-05-31', 'Стажировка как разработчик'),
  (6, 4, 5, 4, '2021-06-01', NULL, 'Перевод на QA'),
  (7, 5, 4, 5, '2023-06-05', '2024-03-31', 'Приём в финансовый отдел'),
  (8, 5, 4, 5, '2024-04-01', '2024-12-31', 'Продление договора'),
  (9, 1, 3, 2, '2019-01-01', '2020-12-31', 'Расширение обязанностей (историческая запись)'),
  (10, 1, 3, 2, '2021-01-01', '2022-12-31', 'Старший HR (история)');

SELECT setval(pg_get_serial_sequence('hr.employees', 'employee_id'),
              (SELECT COALESCE(MAX(employee_id), 1) FROM hr.employees), true);

SELECT setval(pg_get_serial_sequence('hr.departments', 'department_id'),
              (SELECT COALESCE(MAX(department_id), 1) FROM hr.departments), true);

SELECT setval(pg_get_serial_sequence('hr.positions', 'position_id'),
              (SELECT COALESCE(MAX(position_id), 1) FROM hr.positions), true);

SELECT setval(pg_get_serial_sequence('hr.department_positions', 'dept_pos_id'),
              (SELECT COALESCE(MAX(dept_pos_id), 1) FROM hr.department_positions), true);

SELECT setval(pg_get_serial_sequence('hr.assignments', 'assignment_id'),
              (SELECT COALESCE(MAX(assignment_id), 1) FROM hr.assignments), true);

COMMIT;

