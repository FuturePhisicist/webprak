package ru.msu.cmc.webprak.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.cmc.webprak.BaseIntegrationTest;
import ru.msu.cmc.webprak.model.Assignment;
import ru.msu.cmc.webprak.service.exception.BusinessLogicException;
import ru.msu.cmc.webprak.service.exception.EntityNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentServiceTest extends BaseIntegrationTest {

    @Autowired
    private AssignmentService assignmentService;

    @Test
    void findActiveAssignmentByEmployee_shouldReturnActiveAssignment() {
        Optional<Assignment> assignmentOpt = assignmentService.findActiveAssignmentByEmployee(4L);

        assertTrue(assignmentOpt.isPresent());
        assertEquals(6L, assignmentOpt.get().getId());
        assertNull(assignmentOpt.get().getEndDate());
    }

    @Test
    void findActiveAssignmentByEmployee_shouldReturnEmptyWhenNoActiveAssignment() {
        Optional<Assignment> assignmentOpt = assignmentService.findActiveAssignmentByEmployee(5L);

        assertTrue(assignmentOpt.isEmpty());
    }

    @Test
    void findEmployeeHistory_shouldReturnAllAssignmentsInDescendingOrder() {
        List<Assignment> history = assignmentService.findEmployeeHistory(4L);

        assertEquals(2, history.size());
        assertEquals(6L, history.get(0).getId());
        assertEquals(5L, history.get(1).getId());
        assertTrue(history.get(0).getStartDate().isAfter(history.get(1).getStartDate()));
    }

    @Test
    void countOccupiedSlots_shouldReturnCorrectNumberOfActiveAssignments() {
        long occupied = assignmentService.countOccupiedSlots(1L, 1L);

        assertEquals(1, occupied);
    }

    @Test
    void hasFreeSlots_shouldReturnTrueWhenSlotsAvailable() {
        boolean result = assignmentService.hasFreeSlots(5L, 3L);

        assertTrue(result);
    }

    @Test
    void hasFreeSlots_shouldReturnFalseWhenNoSlotsAvailable() {
        boolean result = assignmentService.hasFreeSlots(1L, 1L);

        assertFalse(result);
    }

    @Test
    void hasFreeSlots_shouldThrowWhenDepartmentPositionDoesNotExist() {
        assertThrows(EntityNotFoundException.class,
                () -> assignmentService.hasFreeSlots(3L, 5L));
    }

    @Test
    void assignEmployee_shouldCreateNewAssignmentWhenDataIsValid() {
        Assignment assignment = assignmentService.assignEmployee(
                5L,
                4L,
                5L,
                LocalDate.of(2025, 1, 10),
                "Повторный прием на работу"
        );

        assertNotNull(assignment.getId());
        assertEquals(5L, assignment.getEmployee().getId());
        assertEquals(4L, assignment.getDepartment().getId());
        assertEquals(5L, assignment.getPosition().getId());
        assertEquals(LocalDate.of(2025, 1, 10), assignment.getStartDate());
        assertNull(assignment.getEndDate());
        assertEquals("Повторный прием на работу", assignment.getNote());

        Optional<Assignment> activeAssignment = assignmentService.findActiveAssignmentByEmployee(5L);
        assertTrue(activeAssignment.isPresent());
        assertEquals(assignment.getId(), activeAssignment.get().getId());
    }

    @Test
    void assignEmployee_shouldThrowWhenEmployeeAlreadyHasActiveAssignment() {
        assertThrows(BusinessLogicException.class,
                () -> assignmentService.assignEmployee(
                        1L,
                        4L,
                        5L,
                        LocalDate.of(2025, 1, 10),
                        "Попытка перевода без закрытия старого назначения"
                ));
    }

    @Test
    void assignEmployee_shouldThrowWhenNoFreeSlots() {
        assertThrows(BusinessLogicException.class,
                () -> assignmentService.assignEmployee(
                        5L,
                        1L,
                        1L,
                        LocalDate.of(2025, 1, 10),
                        "Попытка занять уже занятую единственную ставку"
                ));
    }

    @Test
    void assignEmployee_shouldThrowWhenStartDateIsNull() {
        assertThrows(BusinessLogicException.class,
                () -> assignmentService.assignEmployee(
                        5L,
                        4L,
                        5L,
                        null,
                        "Некорректное назначение"
                ));
    }

	@Test
    void assignEmployee_shouldThrowWhenDepartmentPositionNotExists() {
        assertThrows(EntityNotFoundException.class,
                () -> assignmentService.assignEmployee(
                        5L,
                        3L,
                        5L, // нет такой связки
                        LocalDate.now(),
                        "bad"
                ));
    }

	// @Test
 //    void assignEmployee_shouldThrowWhenEmployeeNotFound() {
 //        assertThrows(EntityNotFoundException.class,
 //                () -> assignmentService.assignEmployee(
 //                        999L,
 //                        1L,
 //                        1L,
 //                        LocalDate.now(),
 //                        "bad"
 //                ));
 //    }

    @Test
    void closeActiveAssignment_shouldCloseCurrentAssignment() {
        Assignment closed = assignmentService.closeActiveAssignment(
                4L,
                LocalDate.of(2025, 2, 1)
        );

        assertEquals(6L, closed.getId());
        assertEquals(LocalDate.of(2025, 2, 1), closed.getEndDate());

        Optional<Assignment> activeAssignment = assignmentService.findActiveAssignmentByEmployee(4L);
        assertTrue(activeAssignment.isEmpty());
    }

    @Test
    void closeActiveAssignment_shouldThrowWhenNoActiveAssignmentExists() {
        assertThrows(BusinessLogicException.class,
                () -> assignmentService.closeActiveAssignment(
                        5L,
                        LocalDate.of(2025, 2, 1)
                ));
    }

    @Test
    void closeActiveAssignment_shouldThrowWhenEndDateBeforeStartDate() {
        assertThrows(BusinessLogicException.class,
                () -> assignmentService.closeActiveAssignment(
                        4L,
                        LocalDate.of(2020, 1, 1)
                ));
    }

	@Test
	void transferEmployee_shouldCloseOldAssignmentAndCreateNewOne() {
		Assignment newAssignment = assignmentService.transferEmployee(
				4L,
				4L,
				5L,
				LocalDate.of(2025, 3, 1),
				"Перевод в финансовый отдел"
		);

		assertNotNull(newAssignment.getId());
		assertEquals(4L, newAssignment.getEmployee().getId());
		assertEquals(4L, newAssignment.getDepartment().getId());
		assertEquals(5L, newAssignment.getPosition().getId());
		assertEquals(LocalDate.of(2025, 3, 1), newAssignment.getStartDate());
		assertNull(newAssignment.getEndDate());

		Optional<Assignment> activeAssignment = assignmentService.findActiveAssignmentByEmployee(4L);
		assertTrue(activeAssignment.isPresent());
		assertEquals(newAssignment.getId(), activeAssignment.get().getId());

		List<Assignment> history = assignmentService.findEmployeeHistory(4L);
		assertEquals(3, history.size());
		assertTrue(history.stream().anyMatch(a ->
				a.getId().equals(6L) && LocalDate.of(2025, 2, 28).equals(a.getEndDate())
		));
	}

    @Test
    void transferEmployee_shouldThrowWhenEmployeeHasNoActiveAssignment() {
        assertThrows(BusinessLogicException.class,
                () -> assignmentService.transferEmployee(
                        5L,
                        2L,
                        3L,
                        LocalDate.of(2025, 3, 1),
                        "Некого переводить"
                ));
    }

    @Test
    void transferEmployee_shouldThrowWhenTransferDateConflictsWithCurrentAssignment() {
        assertThrows(BusinessLogicException.class,
                () -> assignmentService.transferEmployee(
                        4L,
                        2L,
                        3L,
                        LocalDate.of(2020, 11, 20),
                        "Слишком ранний перевод"
                ));
    }

    @Test
    void transferEmployee_shouldThrowWhenTransferDateIsNull() {
        assertThrows(BusinessLogicException.class,
                () -> assignmentService.transferEmployee(
                        4L,
                        4L,
                        5L,
                        null,
                        "bad"
                ));
    }
}

