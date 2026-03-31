package ru.msu.cmc.webprak.service;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.model.Assignment;
import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.model.EmployeeStatus;
import ru.msu.cmc.webprak.repository.AssignmentRepository;
import ru.msu.cmc.webprak.repository.EmployeeRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmployeeServiceUnitTest {

    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);

    private final EmployeeService employeeService = new EmployeeService(
            employeeRepository,
            assignmentRepository
    );

    @Test
    void searchByFullName_shouldReturnAllEmployeesWhenQueryIsNull() {
        Employee first = new Employee();
        first.setId(1L);

        Employee second = new Employee();
        second.setId(2L);

        List<Employee> allEmployees = List.of(first, second);
        when(employeeRepository.findAll()).thenReturn(allEmployees);

        List<Employee> result = employeeService.searchByFullName(null);

        assertIterableEquals(allEmployees, result);
    }

    @Test
    void findActiveEmployeesByDepartment_shouldFilterOutInactiveEmployees() {
        Employee activeEmployee = new Employee();
        activeEmployee.setId(1L);
        activeEmployee.setStatus(EmployeeStatus.ACTIVE);

        Employee inactiveEmployee = new Employee();
        inactiveEmployee.setId(5L);
        inactiveEmployee.setStatus(EmployeeStatus.INACTIVE);

        Assignment activeAssignment = new Assignment();
        activeAssignment.setEmployee(activeEmployee);

        Assignment inactiveAssignment = new Assignment();
        inactiveAssignment.setEmployee(inactiveEmployee);

        when(assignmentRepository.findActiveByDepartment(4L))
                .thenReturn(List.of(activeAssignment, inactiveAssignment));

        List<Employee> result = employeeService.findActiveEmployeesByDepartment(4L);

        assertEquals(List.of(activeEmployee), result);
    }

    @Test
    void findActiveEmployeesByPosition_shouldFilterOutInactiveEmployees() {
        Employee activeEmployee = new Employee();
        activeEmployee.setId(3L);
        activeEmployee.setStatus(EmployeeStatus.ACTIVE);

        Employee inactiveEmployee = new Employee();
        inactiveEmployee.setId(5L);
        inactiveEmployee.setStatus(EmployeeStatus.INACTIVE);

        Assignment activeAssignment = new Assignment();
        activeAssignment.setEmployee(activeEmployee);

        Assignment inactiveAssignment = new Assignment();
        inactiveAssignment.setEmployee(inactiveEmployee);

        when(assignmentRepository.findActiveByPosition(3L))
                .thenReturn(List.of(activeAssignment, inactiveAssignment));

        List<Employee> result = employeeService.findActiveEmployeesByPosition(3L);

        assertEquals(List.of(activeEmployee), result);
    }
}

