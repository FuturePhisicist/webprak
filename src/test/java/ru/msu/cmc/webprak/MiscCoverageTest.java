package ru.msu.cmc.webprak;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.controller.form.AssignmentForm;
import ru.msu.cmc.webprak.controller.form.DepartmentForm;
import ru.msu.cmc.webprak.controller.form.DepartmentPositionForm;
import ru.msu.cmc.webprak.controller.form.EmployeeForm;
import ru.msu.cmc.webprak.controller.form.PositionForm;
import ru.msu.cmc.webprak.model.Assignment;
import ru.msu.cmc.webprak.model.Department;
import ru.msu.cmc.webprak.model.EducationLevel;
import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.model.EmployeeStatus;
import ru.msu.cmc.webprak.model.Position;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MiscCoverageTest {

    @Test
    void employeeLifecycleAndFullNameBranchesAreCovered() {
        Employee employee = new Employee();
        employee.setLastName("Иванов");
        employee.setFirstName("Иван");
        employee.setMiddleName(null);

        assertEquals("Иванов Иван", employee.getFullName());

        employee.setMiddleName(" ");
        assertEquals("Иванов Иван", employee.getFullName());

        employee.setMiddleName("Иванович");
        assertEquals("Иванов Иван Иванович", employee.getFullName());

        employee.prePersist();
        assertNotNull(employee.getCreatedAt());
        assertNotNull(employee.getUpdatedAt());
        assertEquals(EmployeeStatus.ACTIVE, employee.getStatus());

        Instant createdAt = employee.getCreatedAt();
        employee.prePersist();
        assertEquals(createdAt, employee.getCreatedAt());

        employee.preUpdate();
        assertNotNull(employee.getUpdatedAt());
    }

    @Test
    void assignmentIsActiveCoversBothBranches() {
        Assignment assignment = new Assignment();
        assertTrue(assignment.isActive());

        assignment.setEndDate(LocalDate.of(2025, 1, 1));
        assertFalse(assignment.isActive());
    }

    @Test
    void formFactoriesAndAccessorsAreCovered() {
        Employee employee = new Employee();
        employee.setLastName("Иванов");
        employee.setFirstName("Иван");
        employee.setMiddleName("Иванович");
        employee.setHomeAddress("Moscow");
        employee.setEducation(EducationLevel.MASTER);
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setStatus(EmployeeStatus.ACTIVE);

        EmployeeForm employeeForm = EmployeeForm.from(employee);
        assertEquals("Иванов", employeeForm.getLastName());
        assertEquals("Иван", employeeForm.getFirstName());
        assertEquals("Иванович", employeeForm.getMiddleName());
        assertEquals("Moscow", employeeForm.getHomeAddress());
        assertEquals(EducationLevel.MASTER, employeeForm.getEducation());
        assertEquals(LocalDate.of(2020, 1, 1), employeeForm.getHireDate());
        assertEquals(EmployeeStatus.ACTIVE, employeeForm.getStatus());

        Department parent = new Department();
        parent.setId(1L);
        Employee manager = new Employee();
        manager.setId(2L);
        Department department = new Department();
        department.setName("IT");
        department.setParentDepartment(parent);
        department.setManager(manager);

        DepartmentForm departmentForm = DepartmentForm.from(department);
        assertEquals("IT", departmentForm.getName());
        assertEquals(1L, departmentForm.getParentDepartmentId());
        assertEquals(2L, departmentForm.getManagerEmployeeId());

        Department rootDepartment = new Department();
        rootDepartment.setName("Root");
        DepartmentForm rootForm = DepartmentForm.from(rootDepartment);
        assertEquals("Root", rootForm.getName());
        assertNull(rootForm.getParentDepartmentId());
        assertNull(rootForm.getManagerEmployeeId());

        departmentForm.setName("HR");
        departmentForm.setParentDepartmentId(3L);
        departmentForm.setManagerEmployeeId(4L);
        assertEquals("HR", departmentForm.getName());
        assertEquals(3L, departmentForm.getParentDepartmentId());
        assertEquals(4L, departmentForm.getManagerEmployeeId());

        Position position = new Position();
        position.setName("Developer");
        position.setResponsibilities("Code");
        PositionForm positionForm = PositionForm.from(position);
        assertEquals("Developer", positionForm.getName());
        assertEquals("Code", positionForm.getResponsibilities());

        positionForm.setName("QA");
        positionForm.setResponsibilities("Test");
        assertEquals("QA", positionForm.getName());
        assertEquals("Test", positionForm.getResponsibilities());
    }

    @Test
    void simpleFormsAccessorsAreCovered() {
        AssignmentForm assignmentForm = new AssignmentForm();
        assignmentForm.setDepartmentId(1L);
        assignmentForm.setPositionId(2L);
        assignmentForm.setStartDate(LocalDate.of(2025, 1, 1));
        assignmentForm.setNote("note");
        assertEquals(1L, assignmentForm.getDepartmentId());
        assertEquals(2L, assignmentForm.getPositionId());
        assertEquals(LocalDate.of(2025, 1, 1), assignmentForm.getStartDate());
        assertEquals("note", assignmentForm.getNote());

        DepartmentPositionForm departmentPositionForm = new DepartmentPositionForm();
        assertEquals(1, departmentPositionForm.getSlotsTotal());
        departmentPositionForm.setPositionId(5L);
        departmentPositionForm.setSlotsTotal(3);
        assertEquals(5L, departmentPositionForm.getPositionId());
        assertEquals(3, departmentPositionForm.getSlotsTotal());
    }
}
