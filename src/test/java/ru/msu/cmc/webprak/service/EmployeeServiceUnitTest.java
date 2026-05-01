package ru.msu.cmc.webprak.service;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.model.Assignment;
import ru.msu.cmc.webprak.model.EducationLevel;
import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.model.EmployeeStatus;
import ru.msu.cmc.webprak.repository.AssignmentRepository;
import ru.msu.cmc.webprak.repository.EmployeeRepository;
import ru.msu.cmc.webprak.service.exception.BusinessLogicException;
import ru.msu.cmc.webprak.service.exception.EntityNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

    @Test
    void detailsMethods_shouldDelegateToRepository() {
        Assignment assignment = new Assignment();
        List<Assignment> history = List.of(assignment);

        when(assignmentRepository.findByEmployeeIdAndEndDateIsNullWithDetails(1L))
                .thenReturn(Optional.of(assignment));
        when(assignmentRepository.findByEmployeeIdOrderByStartDateDescWithDetails(1L))
                .thenReturn(history);

        assertEquals(Optional.of(assignment), employeeService.findCurrentAssignmentWithDetails(1L));
        assertEquals(history, employeeService.getAssignmentHistoryWithDetails(1L));
    }

    @Test
    void create_shouldTrimFieldsAndNormalizeBlankMiddleName() {
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee employee = employeeService.create(
                "  Орлова  ",
                "  Ольга  ",
                "   ",
                "  Moscow  ",
                EducationLevel.MASTER,
                LocalDate.of(2025, 1, 15),
                EmployeeStatus.ACTIVE
        );

        assertEquals("Орлова", employee.getLastName());
        assertEquals("Ольга", employee.getFirstName());
        assertNull(employee.getMiddleName());
        assertEquals("Moscow", employee.getHomeAddress());
    }

    @Test
    void create_shouldKeepNonBlankMiddleName() {
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee employee = employeeService.create(
                "Орлова",
                "Ольга",
                " Павловна ",
                "Moscow",
                EducationLevel.MASTER,
                LocalDate.of(2025, 1, 15),
                EmployeeStatus.ACTIVE
        );

        assertEquals("Павловна", employee.getMiddleName());
    }

    @Test
    void update_shouldModifyExistingEmployee() {
        Employee employee = new Employee();
        employee.setId(8L);

        when(employeeRepository.findById(8L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = employeeService.update(
                8L,
                "Иванова",
                "Ирина",
                null,
                "Utrecht",
                EducationLevel.PHD,
                LocalDate.of(2024, 5, 20),
                EmployeeStatus.INACTIVE
        );

        assertEquals(8L, result.getId());
        assertEquals("Иванова", result.getLastName());
        assertEquals("Ирина", result.getFirstName());
        assertNull(result.getMiddleName());
        assertEquals("Utrecht", result.getHomeAddress());
        assertEquals(EducationLevel.PHD, result.getEducation());
        assertEquals(EmployeeStatus.INACTIVE, result.getStatus());
    }

    @Test
    void update_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(404L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> employeeService.update(
                        404L,
                        "Иванова",
                        "Ирина",
                        null,
                        "Utrecht",
                        EducationLevel.PHD,
                        LocalDate.of(2024, 5, 20),
                        EmployeeStatus.INACTIVE));

        assertEquals("Сотрудник не найден: id=404", exception.getMessage());
    }

    @Test
    void delete_shouldDeleteEmployeeWithoutActiveAssignment() {
        Employee employee = new Employee();
        employee.setId(8L);

        when(employeeRepository.findById(8L)).thenReturn(Optional.of(employee));
        when(assignmentRepository.findByEmployeeIdAndEndDateIsNull(8L)).thenReturn(Optional.empty());

        employeeService.delete(8L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    void delete_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(404L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> employeeService.delete(404L));

        assertEquals("Сотрудник не найден: id=404", exception.getMessage());
    }

    @Test
    void create_shouldValidateRequiredFields() {
        assertValidationError("Фамилия обязательна", null, "Ирина", "Utrecht",
                EducationLevel.PHD, LocalDate.of(2024, 5, 20), EmployeeStatus.ACTIVE);
        assertValidationError("Фамилия обязательна", " ", "Ирина", "Utrecht",
                EducationLevel.PHD, LocalDate.of(2024, 5, 20), EmployeeStatus.ACTIVE);
        assertValidationError("Имя обязательно", "Иванова", null, "Utrecht",
                EducationLevel.PHD, LocalDate.of(2024, 5, 20), EmployeeStatus.ACTIVE);
        assertValidationError("Имя обязательно", "Иванова", " ", "Utrecht",
                EducationLevel.PHD, LocalDate.of(2024, 5, 20), EmployeeStatus.ACTIVE);
        assertValidationError("Домашний адрес обязателен", "Иванова", "Ирина", null,
                EducationLevel.PHD, LocalDate.of(2024, 5, 20), EmployeeStatus.ACTIVE);
        assertValidationError("Домашний адрес обязателен", "Иванова", "Ирина", " ",
                EducationLevel.PHD, LocalDate.of(2024, 5, 20), EmployeeStatus.ACTIVE);
        assertValidationError("Образование обязательно", "Иванова", "Ирина", "Utrecht",
                null, LocalDate.of(2024, 5, 20), EmployeeStatus.ACTIVE);
        assertValidationError("Дата приема обязательна", "Иванова", "Ирина", "Utrecht",
                EducationLevel.PHD, null, EmployeeStatus.ACTIVE);
        assertValidationError("Статус обязателен", "Иванова", "Ирина", "Utrecht",
                EducationLevel.PHD, LocalDate.of(2024, 5, 20), null);
    }

    private void assertValidationError(String expectedMessage,
                                       String lastName,
                                       String firstName,
                                       String homeAddress,
                                       EducationLevel education,
                                       LocalDate hireDate,
                                       EmployeeStatus status) {
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> employeeService.create(lastName, firstName, null, homeAddress, education, hireDate, status));

        assertEquals(expectedMessage, exception.getMessage());
    }
}
