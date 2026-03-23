BEGIN;

DROP SCHEMA IF EXISTS hr CASCADE;
CREATE SCHEMA hr;
SET search_path = hr;

DROP TYPE IF EXISTS employee_status CASCADE;
DROP TYPE IF EXISTS education_level CASCADE;

CREATE TYPE employee_status AS ENUM ('ACTIVE', 'INACTIVE');
CREATE TYPE education_level AS ENUM ('SECONDARY', 'VOCATIONAL', 'BACHELOR', 'MASTER', 'PHD');

CREATE TABLE employees (
  employee_id   BIGSERIAL PRIMARY KEY,
  last_name     VARCHAR(100) NOT NULL,
  first_name    VARCHAR(100) NOT NULL,
  middle_name   VARCHAR(100),
  home_address  TEXT NOT NULL,
  education     education_level NOT NULL,
  hire_date     DATE NOT NULL,
  status        employee_status NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE departments (
  department_id        BIGSERIAL PRIMARY KEY,
  name                 VARCHAR(200) NOT NULL UNIQUE,
  parent_department_id BIGINT NULL,
  manager_employee_id  BIGINT NULL,
  CONSTRAINT fk_departments_parent
    FOREIGN KEY (parent_department_id) REFERENCES departments(department_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_departments_manager
    FOREIGN KEY (manager_employee_id) REFERENCES employees(employee_id)
    ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE positions (
  position_id      BIGSERIAL PRIMARY KEY,
  name             VARCHAR(200) NOT NULL UNIQUE,
  responsibilities TEXT NOT NULL
);

CREATE TABLE department_positions (
  dept_pos_id   BIGSERIAL PRIMARY KEY,
  department_id BIGINT NOT NULL,
  position_id   BIGINT NOT NULL,
  slots_total   INT NOT NULL CHECK (slots_total >= 0),
  CONSTRAINT fk_deptpos_department
    FOREIGN KEY (department_id) REFERENCES departments(department_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_deptpos_position
    FOREIGN KEY (position_id) REFERENCES positions(position_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT uq_deptpos_unique UNIQUE (department_id, position_id)
);

CREATE TABLE assignments (
  assignment_id BIGSERIAL PRIMARY KEY,
  employee_id   BIGINT NOT NULL,
  department_id BIGINT NOT NULL,
  position_id   BIGINT NOT NULL,
  start_date    DATE NOT NULL,
  end_date      DATE NULL,
  note          TEXT NULL,

  CONSTRAINT fk_assign_employee
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT fk_assign_department
    FOREIGN KEY (department_id) REFERENCES departments(department_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT fk_assign_position
    FOREIGN KEY (position_id) REFERENCES positions(position_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT ck_assign_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_employees_last_first ON employees(last_name, first_name);

CREATE UNIQUE INDEX uq_active_assignment_per_employee
  ON assignments(employee_id)
  WHERE end_date IS NULL;

CREATE INDEX idx_assignments_department ON assignments(department_id);
CREATE INDEX idx_assignments_position ON assignments(position_id);
CREATE INDEX idx_assignments_employee_dates ON assignments(employee_id, start_date DESC);

COMMIT;

