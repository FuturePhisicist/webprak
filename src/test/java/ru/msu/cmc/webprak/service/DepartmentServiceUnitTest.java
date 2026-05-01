package ru.msu.cmc.webprak.service;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.model.Department;
import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.repository.AssignmentRepository;
import ru.msu.cmc.webprak.repository.DepartmentRepository;
import ru.msu.cmc.webprak.repository.EmployeeRepository;
import ru.msu.cmc.webprak.service.exception.BusinessLogicException;
import ru.msu.cmc.webprak.service.exception.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DepartmentServiceUnitTest {
    private final DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
    private final AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);
    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);

    private final DepartmentService departmentService = new DepartmentService(
            departmentRepository,
            assignmentRepository,
            employeeRepository
    );

    @Test
    void create_shouldCreateRootDepartmentWithoutManager() {
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Department department = departmentService.create("  Архив  ", null, null);

        assertEquals("Архив", department.getName());
        assertNull(department.getParentDepartment());
        assertNull(department.getManager());
    }

    @Test
    void create_shouldCreateDepartmentWithParentAndManager() {
        Department parent = new Department();
        parent.setId(1L);
        Employee manager = new Employee();
        manager.setId(2L);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Department department = departmentService.create("Развитие", 1L, 2L);

        assertEquals(parent, department.getParentDepartment());
        assertEquals(manager, department.getManager());
    }

    @Test
    void create_shouldValidateName() {
        BusinessLogicException nullName = assertThrows(BusinessLogicException.class,
                () -> departmentService.create(null, null, null));
        BusinessLogicException blankName = assertThrows(BusinessLogicException.class,
                () -> departmentService.create(" ", null, null));

        assertEquals("Название подразделения обязательно", nullName.getMessage());
        assertEquals("Название подразделения обязательно", blankName.getMessage());
    }

    @Test
    void create_shouldThrowWhenParentNotFound() {
        when(departmentRepository.findById(404L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> departmentService.create("Развитие", 404L, null));

        assertEquals("Головное подразделение не найдено: id=404", exception.getMessage());
    }

    @Test
    void create_shouldThrowWhenManagerNotFound() {
        when(employeeRepository.findById(404L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> departmentService.create("Развитие", null, 404L));

        assertEquals("Руководитель не найден: id=404", exception.getMessage());
    }

    @Test
    void update_shouldUpdateExistingDepartment() {
        Department department = new Department();
        department.setId(3L);
        Department parent = new Department();
        parent.setId(1L);
        Employee manager = new Employee();
        manager.setId(2L);

        when(departmentRepository.findById(3L)).thenReturn(Optional.of(department));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Department result = departmentService.update(3L, "Поддержка", 1L, 2L);

        assertEquals("Поддержка", result.getName());
        assertEquals(parent, result.getParentDepartment());
        assertEquals(manager, result.getManager());
    }

    @Test
    void update_shouldAllowRootDepartmentWithoutManager() {
        Department department = new Department();
        department.setId(3L);

        when(departmentRepository.findById(3L)).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Department result = departmentService.update(3L, "Поддержка", null, null);

        assertEquals("Поддержка", result.getName());
        assertNull(result.getParentDepartment());
        assertNull(result.getManager());
    }

    @Test
    void update_shouldThrowWhenDepartmentNotFound() {
        when(departmentRepository.findById(404L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> departmentService.update(404L, "Поддержка", null, null));

        assertEquals("Подразделение не найдено: id=404", exception.getMessage());
    }

    @Test
    void update_shouldThrowWhenParentIsSameDepartment() {
        Department department = new Department();
        department.setId(3L);
        when(departmentRepository.findById(3L)).thenReturn(Optional.of(department));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> departmentService.update(3L, "Поддержка", 3L, null));

        assertEquals("Подразделение не может быть головным для самого себя", exception.getMessage());
    }

    @Test
    void delete_shouldDeleteEmptyLeafDepartment() {
        Department department = new Department();
        department.setId(6L);

        when(departmentRepository.findById(6L)).thenReturn(Optional.of(department));
        when(departmentRepository.findByParentDepartmentId(6L)).thenReturn(List.of());
        when(assignmentRepository.countActiveByDepartment(6L)).thenReturn(0L);

        departmentService.delete(6L);

        verify(departmentRepository).delete(department);
    }

    @Test
    void delete_shouldThrowWhenDepartmentNotFound() {
        when(departmentRepository.findById(404L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> departmentService.delete(404L));

        assertEquals("Подразделение не найдено: id=404", exception.getMessage());
    }

    @Test
    void delete_shouldThrowWhenDepartmentHasChildren() {
        Department department = new Department();
        department.setId(1L);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.findByParentDepartmentId(1L)).thenReturn(List.of(new Department()));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> departmentService.delete(1L));

        assertEquals("Нельзя удалить подразделение с внутренними подразделениями", exception.getMessage());
    }

    @Test
    void delete_shouldThrowWhenDepartmentHasActiveEmployees() {
        Department department = new Department();
        department.setId(5L);
        when(departmentRepository.findById(5L)).thenReturn(Optional.of(department));
        when(departmentRepository.findByParentDepartmentId(5L)).thenReturn(List.of());
        when(assignmentRepository.countActiveByDepartment(5L)).thenReturn(1L);

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> departmentService.delete(5L));

        assertEquals("Нельзя удалить подразделение с активными сотрудниками", exception.getMessage());
    }
}
