package ru.msu.cmc.webprak.service;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.model.Department;
import ru.msu.cmc.webprak.model.DepartmentPosition;
import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.model.Position;
import ru.msu.cmc.webprak.repository.AssignmentRepository;
import ru.msu.cmc.webprak.repository.DepartmentPositionRepository;
import ru.msu.cmc.webprak.repository.DepartmentRepository;
import ru.msu.cmc.webprak.repository.EmployeeRepository;
import ru.msu.cmc.webprak.repository.PositionRepository;
import ru.msu.cmc.webprak.service.exception.BusinessLogicException;
import ru.msu.cmc.webprak.service.exception.EntityNotFoundException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// These tests are separate because SpringBoot rollbacks even if the request was RO
// But by that time the connection is closed because the execution of the program is irrestorable for the test
// Hacky, yep

class AssignmentServiceUnitTest {

    private final AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);
    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
    private final PositionRepository positionRepository = mock(PositionRepository.class);
    private final DepartmentPositionRepository departmentPositionRepository = mock(DepartmentPositionRepository.class);

    private final AssignmentService assignmentService = new AssignmentService(
            assignmentRepository,
            employeeRepository,
            departmentRepository,
            positionRepository,
            departmentPositionRepository
    );

    @Test
    void requireEmployee_shouldReturnEmployeeWhenExists() {
        Employee employee = new Employee();
        employee.setId(1L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Employee result = assignmentService.requireEmployee(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void requireEmployee_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> assignmentService.requireEmployee(999L)
        );

        assertEquals("Сотрудник не найден: id=999", ex.getMessage());
    }

    @Test
    void requireDepartment_shouldReturnDepartmentWhenExists() {
        Department department = new Department();
        department.setId(4L);

        when(departmentRepository.findById(4L)).thenReturn(Optional.of(department));

        Department result = assignmentService.requireDepartment(4L);

        assertEquals(4L, result.getId());
    }

    @Test
    void requireDepartment_shouldThrowWhenDepartmentNotFound() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> assignmentService.requireDepartment(999L)
        );

        assertEquals("Подразделение не найдено: id=999", ex.getMessage());
    }

    @Test
    void requirePosition_shouldReturnPositionWhenExists() {
        Position position = new Position();
        position.setId(5L);

        when(positionRepository.findById(5L)).thenReturn(Optional.of(position));

        Position result = assignmentService.requirePosition(5L);

        assertEquals(5L, result.getId());
    }

    @Test
    void requirePosition_shouldThrowWhenPositionNotFound() {
        when(positionRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> assignmentService.requirePosition(999L)
        );

        assertEquals("Должность не найдена: id=999", ex.getMessage());
    }

    @Test
    void requireDepartmentPosition_shouldReturnDepartmentPositionWhenExists() {
        Department department = new Department();
        department.setId(4L);

        Position position = new Position();
        position.setId(5L);

        DepartmentPosition dp = new DepartmentPosition();
        dp.setDepartment(department);
        dp.setPosition(position);
        dp.setSlotsTotal(2);

        when(departmentPositionRepository.findByDepartmentIdAndPositionId(4L, 5L))
                .thenReturn(Optional.of(dp));

        DepartmentPosition result = assignmentService.requireDepartmentPosition(4L, 5L);

        assertEquals(2, result.getSlotsTotal());
    }

    @Test
    void requireDepartmentPosition_shouldThrowWhenDepartmentPositionNotFound() {
        when(departmentPositionRepository.findByDepartmentIdAndPositionId(3L, 5L))
                .thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> assignmentService.requireDepartmentPosition(3L, 5L)
        );

        assertEquals("Для подразделения id=3 не предусмотрена должность id=5", ex.getMessage());
    }

    @Test
    void validateAssignmentRequest_shouldThrowWhenStartDateIsNull() {
        BusinessLogicException ex = assertThrows(
                BusinessLogicException.class,
                () -> assignmentService.validateAssignmentRequest(5L, 4L, 5L, null)
        );

        assertEquals("Дата назначения не может быть null", ex.getMessage());
    }

    @Test
    void validateAssignmentRequest_shouldThrowWhenEmployeeAlreadyHasActiveAssignment() {
        Employee employee = new Employee();
        employee.setId(1L);

        Department department = new Department();
        department.setId(4L);

        Position position = new Position();
        position.setId(5L);

        DepartmentPosition dp = new DepartmentPosition();
        dp.setDepartment(department);
        dp.setPosition(position);
        dp.setSlotsTotal(2);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(4L)).thenReturn(Optional.of(department));
        when(positionRepository.findById(5L)).thenReturn(Optional.of(position));
        when(departmentPositionRepository.findByDepartmentIdAndPositionId(4L, 5L))
                .thenReturn(Optional.of(dp));
        when(assignmentRepository.findByEmployeeIdAndEndDateIsNull(1L))
                .thenReturn(Optional.of(new ru.msu.cmc.webprak.model.Assignment()));

        BusinessLogicException ex = assertThrows(
                BusinessLogicException.class,
                () -> assignmentService.validateAssignmentRequest(
                        1L, 4L, 5L, LocalDate.of(2025, 1, 10)
                )
        );

        assertEquals("У сотрудника уже есть активное назначение", ex.getMessage());
    }

    @Test
    void validateAssignmentRequest_shouldThrowWhenNoFreeSlotsAvailable() {
        Employee employee = new Employee();
        employee.setId(8L);

        Department department = new Department();
        department.setId(1L);

        Position position = new Position();
        position.setId(1L);

        DepartmentPosition dp = new DepartmentPosition();
        dp.setDepartment(department);
        dp.setPosition(position);
        dp.setSlotsTotal(1);

        when(employeeRepository.findById(8L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));
        when(departmentPositionRepository.findByDepartmentIdAndPositionId(1L, 1L))
                .thenReturn(Optional.of(dp));
        when(assignmentRepository.findByEmployeeIdAndEndDateIsNull(8L))
                .thenReturn(Optional.empty());
        when(assignmentRepository.countActiveByDepartmentAndPosition(1L, 1L))
                .thenReturn(1L);

        BusinessLogicException ex = assertThrows(
                BusinessLogicException.class,
                () -> assignmentService.validateAssignmentRequest(
                        8L, 1L, 1L, LocalDate.of(2025, 1, 10)
                )
        );

        assertEquals("Нет свободных ставок для departmentId=1, positionId=1", ex.getMessage());
    }

    @Test
    void closeActiveAssignment_shouldThrowWhenEndDateIsNull() {
        BusinessLogicException ex = assertThrows(
                BusinessLogicException.class,
                () -> assignmentService.closeActiveAssignment(1L, null)
        );

        assertEquals("Дата завершения не может быть null", ex.getMessage());
    }

    @Test
    void transferEmployee_shouldThrowWhenNoFreeSlotsAvailable() {
        Employee employee = new Employee();
        employee.setId(4L);

        Department department = new Department();
        department.setId(2L);

        Position position = new Position();
        position.setId(3L);

        DepartmentPosition dp = new DepartmentPosition();
        dp.setDepartment(department);
        dp.setPosition(position);
        dp.setSlotsTotal(1);

        ru.msu.cmc.webprak.model.Assignment currentAssignment = new ru.msu.cmc.webprak.model.Assignment();
        currentAssignment.setEmployee(employee);
        currentAssignment.setDepartment(department);
        currentAssignment.setPosition(position);
        currentAssignment.setStartDate(LocalDate.of(2024, 1, 1));

        when(assignmentRepository.findByEmployeeIdAndEndDateIsNull(4L))
                .thenReturn(Optional.of(currentAssignment));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(department));
        when(positionRepository.findById(3L)).thenReturn(Optional.of(position));
        when(departmentPositionRepository.findByDepartmentIdAndPositionId(2L, 3L))
                .thenReturn(Optional.of(dp));
        when(assignmentRepository.countActiveByDepartmentAndPosition(2L, 3L))
                .thenReturn(1L);

        BusinessLogicException ex = assertThrows(
                BusinessLogicException.class,
                () -> assignmentService.transferEmployee(
                        4L, 2L, 3L, LocalDate.of(2025, 3, 1), "Перевод без свободной ставки"
                )
        );

        assertEquals("Нет свободных ставок для departmentId=2, positionId=3", ex.getMessage());
    }
}
