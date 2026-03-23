package ru.msu.cmc.webprak.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.cmc.webprak.BaseIntegrationTest;
import ru.msu.cmc.webprak.model.Assignment;
import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.model.EmployeeStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceTest extends BaseIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Test
    void findAllActive_shouldReturnOnlyActiveEmployees() {
        List<Employee> employees = employeeService.findAllActive();

        assertEquals(4, employees.size());
        assertTrue(employees.stream().allMatch(e -> e.getStatus() == EmployeeStatus.ACTIVE));
    }

    @Test
    void searchByFullName_shouldFindEmployeeByPartialName() {
        List<Employee> employees = employeeService.searchByFullName("Иван");

        assertFalse(employees.isEmpty());
        assertTrue(employees.stream().anyMatch(e -> e.getLastName().equals("Иванов")));
    }

    @Test
    void searchByFullName_shouldReturnEmptyListWhenNothingFound() {
        List<Employee> employees = employeeService.searchByFullName("Несуществующий Сотрудник");

        assertNotNull(employees);
        assertTrue(employees.isEmpty());
    }

    @Test
    void searchByFullName_blankQueryShouldReturnAllEmployees() {
        List<Employee> employees = employeeService.searchByFullName("   ");

        assertEquals(5, employees.size());
    }

    @Test
    void findCurrentAssignment_shouldReturnActiveAssignment() {
        Optional<Assignment> assignmentOpt = employeeService.findCurrentAssignment(1L);

        assertTrue(assignmentOpt.isPresent());

        Assignment assignment = assignmentOpt.get();
        assertEquals(1L, assignment.getEmployee().getId());
        assertNull(assignment.getEndDate());
        assertEquals(3L, assignment.getDepartment().getId());
        assertEquals(2L, assignment.getPosition().getId());
    }

    @Test
    void findCurrentAssignment_shouldReturnEmptyForEmployeeWithoutActiveAssignment() {
        Optional<Assignment> assignmentOpt = employeeService.findCurrentAssignment(5L);

        assertTrue(assignmentOpt.isEmpty());
    }

    @Test
    void getAssignmentHistory_shouldReturnEmployeeHistoryInDescendingOrder() {
        List<Assignment> history = employeeService.getAssignmentHistory(1L);

        assertEquals(3, history.size());

        assertEquals(10L, history.get(0).getId());
        assertEquals(9L, history.get(1).getId());
        assertEquals(2L, history.get(2).getId());

        assertTrue(history.get(0).getStartDate().isAfter(history.get(1).getStartDate())
                || history.get(0).getStartDate().isEqual(history.get(1).getStartDate()));
        assertTrue(history.get(1).getStartDate().isAfter(history.get(2).getStartDate())
                || history.get(1).getStartDate().isEqual(history.get(2).getStartDate()));
    }

    @Test
    void findActiveEmployeesByDepartment_shouldReturnOnlyActiveEmployeesFromDepartment() {
        List<Employee> employees = employeeService.findActiveEmployeesByDepartment(5L);

        assertEquals(1, employees.size());
        assertEquals("Ким", employees.get(0).getLastName());
        assertEquals(EmployeeStatus.ACTIVE, employees.get(0).getStatus());
    }

    @Test
    void findActiveEmployeesByPosition_shouldReturnOnlyActiveEmployeesForPosition() {
        List<Employee> employees = employeeService.findActiveEmployeesByPosition(3L);

        assertEquals(1, employees.size());
        assertEquals("Сидорова", employees.get(0).getLastName());
        assertEquals(EmployeeStatus.ACTIVE, employees.get(0).getStatus());
    }
}

