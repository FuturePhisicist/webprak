BEGIN;

-- Recreate schema

DROP SCHEMA IF EXISTS hr CASCADE;
CREATE SCHEMA hr;
SET search_path = hr;

-- Enum types

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'EmployeeStatus') THEN
    CREATE TYPE EmployeeStatus AS ENUM ('ACTIVE', 'INACTIVE');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'EducationLevel') THEN
    CREATE TYPE EducationLevel AS ENUM ('SECONDARY', 'VOCATIONAL', 'BACHELOR', 'MASTER', 'PHD');
  END IF;
END$$;

-- Tables

CREATE TABLE Employees (
  employee_id   BIGSERIAL PRIMARY KEY,
  last_name     VARCHAR(100) NOT NULL,
  first_name    VARCHAR(100) NOT NULL,
  middle_name   VARCHAR(100),
  home_address  TEXT NOT NULL,
  education     EducationLevel NOT NULL,
  hire_date     DATE NOT NULL,
  status        EmployeeStatus NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE Departments (
  department_id        BIGSERIAL PRIMARY KEY,
  name                 VARCHAR(200) NOT NULL UNIQUE,
  parent_department_id BIGINT NULL,
  manager_employee_id  BIGINT NULL,
  CONSTRAINT fk_departments_parent
    FOREIGN KEY (parent_department_id) REFERENCES Departments(department_id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_departments_manager
    FOREIGN KEY (manager_employee_id) REFERENCES Employees(employee_id)
    ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE Positions (
  position_id      BIGSERIAL PRIMARY KEY,
  name             VARCHAR(200) NOT NULL UNIQUE,
  responsibilities TEXT NOT NULL
);

CREATE TABLE DepartmentPositions (
  dept_pos_id   BIGSERIAL PRIMARY KEY,
  department_id BIGINT NOT NULL,
  position_id   BIGINT NOT NULL,
  slots_total   INT NOT NULL CHECK (slots_total >= 0),
  CONSTRAINT fk_deptpos_department
    FOREIGN KEY (department_id) REFERENCES Departments(department_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_deptpos_position
    FOREIGN KEY (position_id) REFERENCES Positions(position_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT uq_deptpos_unique UNIQUE (department_id, position_id)
);

CREATE TABLE Assignments (
  assignment_id BIGSERIAL PRIMARY KEY,
  employee_id   BIGINT NOT NULL,
  department_id BIGINT NOT NULL,
  position_id   BIGINT NOT NULL,
  start_date    DATE NOT NULL,
  end_date      DATE NULL,
  note          TEXT NULL,

  CONSTRAINT fk_assign_employee
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT fk_assign_department
    FOREIGN KEY (department_id) REFERENCES Departments(department_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT fk_assign_position
    FOREIGN KEY (position_id) REFERENCES Positions(position_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT ck_assign_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

-- Indexes

-- For frequent search by name
CREATE INDEX idx_employees_last_first ON Employees(last_name, first_name);

-- Active assignment per employee: only one row with end_date IS NULL
CREATE UNIQUE INDEX uq_active_assignment_per_employee
  ON Assignments(employee_id)
  WHERE end_date IS NULL;

-- Helpful indexes for filtering
CREATE INDEX idx_assignments_department ON Assignments(department_id);
CREATE INDEX idx_assignments_position ON Assignments(position_id);
CREATE INDEX idx_assignments_employee_dates ON Assignments(employee_id, start_date DESC);

COMMIT;

