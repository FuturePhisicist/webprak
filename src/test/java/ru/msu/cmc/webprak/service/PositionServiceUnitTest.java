package ru.msu.cmc.webprak.service;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.model.Department;
import ru.msu.cmc.webprak.model.DepartmentPosition;
import ru.msu.cmc.webprak.model.Position;
import ru.msu.cmc.webprak.repository.AssignmentRepository;
import ru.msu.cmc.webprak.repository.DepartmentPositionRepository;
import ru.msu.cmc.webprak.repository.DepartmentRepository;
import ru.msu.cmc.webprak.repository.PositionRepository;
import ru.msu.cmc.webprak.service.exception.BusinessLogicException;
import ru.msu.cmc.webprak.service.exception.EntityNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PositionServiceUnitTest {
    private final PositionRepository positionRepository = mock(PositionRepository.class);
    private final DepartmentPositionRepository departmentPositionRepository = mock(DepartmentPositionRepository.class);
    private final AssignmentRepository assignmentRepository = mock(AssignmentRepository.class);
    private final DepartmentRepository departmentRepository = mock(DepartmentRepository.class);

    private final PositionService positionService = new PositionService(
            positionRepository,
            departmentPositionRepository,
            assignmentRepository,
            departmentRepository
    );

    @Test
    void addPositionToDepartment_shouldCreateStaffingEntry() {
        Department department = new Department();
        department.setId(5L);
        Position position = new Position();
        position.setId(6L);

        when(departmentPositionRepository.findByDepartmentIdAndPositionId(5L, 6L)).thenReturn(Optional.empty());
        when(departmentRepository.findById(5L)).thenReturn(Optional.of(department));
        when(positionRepository.findById(6L)).thenReturn(Optional.of(position));
        when(departmentPositionRepository.save(any(DepartmentPosition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DepartmentPosition result = positionService.addPositionToDepartment(5L, 6L, 2);

        assertEquals(department, result.getDepartment());
        assertEquals(position, result.getPosition());
        assertEquals(2, result.getSlotsTotal());
    }

    @Test
    void addPositionToDepartment_shouldValidateSlots() {
        BusinessLogicException nullSlots = assertThrows(BusinessLogicException.class,
                () -> positionService.addPositionToDepartment(5L, 6L, null));
        BusinessLogicException zeroSlots = assertThrows(BusinessLogicException.class,
                () -> positionService.addPositionToDepartment(5L, 6L, 0));

        assertEquals("Количество ставок должно быть положительным", nullSlots.getMessage());
        assertEquals("Количество ставок должно быть положительным", zeroSlots.getMessage());
    }

    @Test
    void addPositionToDepartment_shouldThrowWhenDuplicateExists() {
        when(departmentPositionRepository.findByDepartmentIdAndPositionId(5L, 6L))
                .thenReturn(Optional.of(new DepartmentPosition()));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> positionService.addPositionToDepartment(5L, 6L, 1));

        assertEquals("Должность уже предусмотрена в подразделении", exception.getMessage());
    }

    @Test
    void addPositionToDepartment_shouldThrowWhenDepartmentNotFound() {
        when(departmentPositionRepository.findByDepartmentIdAndPositionId(404L, 6L)).thenReturn(Optional.empty());
        when(departmentRepository.findById(404L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> positionService.addPositionToDepartment(404L, 6L, 1));

        assertEquals("Подразделение не найдено: id=404", exception.getMessage());
    }

    @Test
    void addPositionToDepartment_shouldThrowWhenPositionNotFound() {
        Department department = new Department();
        department.setId(5L);
        when(departmentPositionRepository.findByDepartmentIdAndPositionId(5L, 404L)).thenReturn(Optional.empty());
        when(departmentRepository.findById(5L)).thenReturn(Optional.of(department));
        when(positionRepository.findById(404L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> positionService.addPositionToDepartment(5L, 404L, 1));

        assertEquals("Должность не найдена: id=404", exception.getMessage());
    }

    @Test
    void removePositionFromDepartment_shouldDeleteUnusedStaffingEntry() {
        DepartmentPosition departmentPosition = new DepartmentPosition();
        when(departmentPositionRepository.findByDepartmentIdAndPositionId(5L, 6L))
                .thenReturn(Optional.of(departmentPosition));
        when(assignmentRepository.countActiveByDepartmentAndPosition(5L, 6L)).thenReturn(0L);

        positionService.removePositionFromDepartment(5L, 6L);

        verify(departmentPositionRepository).delete(departmentPosition);
    }

    @Test
    void removePositionFromDepartment_shouldThrowWhenEntryNotFound() {
        when(departmentPositionRepository.findByDepartmentIdAndPositionId(5L, 6L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> positionService.removePositionFromDepartment(5L, 6L));

        assertEquals("Для подразделения id=5 не предусмотрена должность id=6", exception.getMessage());
    }

    @Test
    void removePositionFromDepartment_shouldThrowWhenActiveAssignmentsExist() {
        when(departmentPositionRepository.findByDepartmentIdAndPositionId(5L, 6L))
                .thenReturn(Optional.of(new DepartmentPosition()));
        when(assignmentRepository.countActiveByDepartmentAndPosition(5L, 6L)).thenReturn(1L);

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> positionService.removePositionFromDepartment(5L, 6L));

        assertEquals("Нельзя удалить должность из подразделения: есть активные назначения", exception.getMessage());
    }

    @Test
    void create_shouldCreatePosition() {
        when(positionRepository.save(any(Position.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Position position = positionService.create("  Инженер  ", "  Пишет код  ");

        assertEquals("Инженер", position.getName());
        assertEquals("Пишет код", position.getResponsibilities());
    }

    @Test
    void create_shouldValidatePositionFields() {
        assertPositionValidation("Название должности обязательно", null, "Описание");
        assertPositionValidation("Название должности обязательно", " ", "Описание");
        assertPositionValidation("Описание обязанностей обязательно", "Инженер", null);
        assertPositionValidation("Описание обязанностей обязательно", "Инженер", " ");
    }

    @Test
    void update_shouldModifyExistingPosition() {
        Position position = new Position();
        position.setId(6L);
        when(positionRepository.findById(6L)).thenReturn(Optional.of(position));
        when(positionRepository.save(any(Position.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Position result = positionService.update(6L, "Инженер", "Пишет код");

        assertEquals(6L, result.getId());
        assertEquals("Инженер", result.getName());
        assertEquals("Пишет код", result.getResponsibilities());
    }

    @Test
    void update_shouldThrowWhenPositionNotFound() {
        when(positionRepository.findById(404L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> positionService.update(404L, "Инженер", "Пишет код"));

        assertEquals("Должность не найдена: id=404", exception.getMessage());
    }

    @Test
    void delete_shouldDeletePositionWithoutActiveAssignments() {
        Position position = new Position();
        position.setId(6L);
        when(positionRepository.findById(6L)).thenReturn(Optional.of(position));
        when(assignmentRepository.countActiveByPosition(6L)).thenReturn(0L);

        positionService.delete(6L);

        verify(positionRepository).delete(position);
    }

    @Test
    void delete_shouldThrowWhenPositionNotFound() {
        when(positionRepository.findById(404L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> positionService.delete(404L));

        assertEquals("Должность не найдена: id=404", exception.getMessage());
    }

    @Test
    void delete_shouldThrowWhenActiveAssignmentsExist() {
        Position position = new Position();
        position.setId(6L);
        when(positionRepository.findById(6L)).thenReturn(Optional.of(position));
        when(assignmentRepository.countActiveByPosition(6L)).thenReturn(1L);

        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> positionService.delete(6L));

        assertEquals("Нельзя удалить должность с активными назначениями", exception.getMessage());
    }

    private void assertPositionValidation(String expectedMessage, String name, String responsibilities) {
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> positionService.create(name, responsibilities));

        assertEquals(expectedMessage, exception.getMessage());
    }
}
