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

    // @Test
    // void assignEmployee_shouldThrowWhenNoFreeSlots() {
    //     assertThrows(BusinessLogicException.class,
    //             () -> assignmentService.assignEmployee(
    //                     5L,
    //                     1L,
    //                     1L,
    //                     LocalDate.of(2025, 1, 10),
    //                     "Попытка занять уже занятую единственную ставку"
    //             ));
    // }

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

	// @Test
 //    void assignEmployee_shouldThrowWhenDepartmentPositionNotExists() {
 //        assertThrows(EntityNotFoundException.class,
 //                () -> assignmentService.assignEmployee(
 //                        5L,
 //                        3L,
 //                        5L, // нет такой связки
 //                        LocalDate.now(),
 //                        "bad"
 //                ));
 //    }

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

	// @Test
	// void assignEmployee_shouldThrowWhenEmployeeNotFound() {
	// 	EntityNotFoundException ex = assertThrows(
	// 			EntityNotFoundException.class,
	// 			() -> assignmentService.assignEmployee(
	// 					999L,
	// 					4L,
	// 					5L,
	// 					LocalDate.of(2025, 1, 10),
	// 					"Попытка назначения несуществующего сотрудника"
	// 			)
	// 	);
	//
	// 	assertEquals("Сотрудник не найден: id=999", ex.getMessage());
	// }

	// @Test
	// void assignEmployee_shouldThrowWhenDepartmentNotFound() {
	// 	EntityNotFoundException ex = assertThrows(
	// 			EntityNotFoundException.class,
	// 			() -> assignmentService.assignEmployee(
	// 					5L,
	// 					999L,
	// 					5L,
	// 					LocalDate.of(2025, 1, 10),
	// 					"Попытка назначения в несуществующее подразделение"
	// 			)
	// 	);
	//
	// 	assertEquals("Подразделение не найдено: id=999", ex.getMessage());
	// }

	// @Test
	// void assignEmployee_shouldThrowWhenPositionNotFound() {
	// 	EntityNotFoundException ex = assertThrows(
	// 			EntityNotFoundException.class,
	// 			() -> assignmentService.assignEmployee(
	// 					5L,
	// 					4L,
	// 					999L,
	// 					LocalDate.of(2025, 1, 10),
	// 					"Попытка назначения на несуществующую должность"
	// 			)
	// 	);
	//
	// 	assertEquals("Должность не найдена: id=999", ex.getMessage());
	// }

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

	// requireEmployee
	// @Test
	// void requireEmployee_shouldReturnEmployeeWhenExists() {
	// 	assertEquals(1L, assignmentService.requireEmployee(1L).getId());
	// }

	// @Test
	// void requireEmployee_shouldThrowWhenEmployeeNotFound() {
	// 	EntityNotFoundException ex = assertThrows(
	// 			EntityNotFoundException.class,
	// 			() -> assignmentService.requireEmployee(999L)
	// 	);
	//
	// 	assertEquals("Сотрудник не найден: id=999", ex.getMessage());
	// }

	// requireDepartment
	// @Test
	// void requireDepartment_shouldReturnDepartmentWhenExists() {
	// 	assertEquals(4L, assignmentService.requireDepartment(4L).getId());
	// }
	//
	// @Test
	// void requireDepartment_shouldThrowWhenDepartmentNotFound() {
	// 	EntityNotFoundException ex = assertThrows(
	// 			EntityNotFoundException.class,
	// 			() -> assignmentService.requireDepartment(999L)
	// 	);
	//
	// 	assertEquals("Подразделение не найдено: id=999", ex.getMessage());
	// }

	// requirePosition
	// @Test
	// void requirePosition_shouldReturnPositionWhenExists() {
	// 	assertEquals(5L, assignmentService.requirePosition(5L).getId());
	// }
	//
	// @Test
	// void requirePosition_shouldThrowWhenPositionNotFound() {
	// 	EntityNotFoundException ex = assertThrows(
	// 			EntityNotFoundException.class,
	// 			() -> assignmentService.requirePosition(999L)
	// 	);
	//
	// 	assertEquals("Должность не найдена: id=999", ex.getMessage());
	// }

	// requireDepartmentPosition
	// @Test
	// void requireDepartmentPosition_shouldReturnDepartmentPositionWhenExists() {
	// 	assertNotNull(assignmentService.requireDepartmentPosition(4L, 5L));
	// 	assertEquals(2, assignmentService.requireDepartmentPosition(4L, 5L).getSlotsTotal());
	// }
	//
	// @Test
	// void requireDepartmentPosition_shouldThrowWhenDepartmentPositionNotExists() {
	// 	EntityNotFoundException ex = assertThrows(
	// 			EntityNotFoundException.class,
	// 			() -> assignmentService.requireDepartmentPosition(3L, 5L)
	// 	);
	//
	// 	assertEquals("Для подразделения id=3 не предусмотрена должность id=5", ex.getMessage());
	// }

	// validateAssignmentRequest
	// @Test
	// void validateAssignmentRequest_shouldPassWhenDataIsValid() {
	// 	assertDoesNotThrow(() ->
	// 			assignmentService.validateAssignmentRequest(
	// 					5L,
	// 					4L,
	// 					5L,
	// 					LocalDate.of(2025, 1, 10)
	// 			)
	// 	);
	// }

	// @Test
	// void validateAssignmentRequest_shouldThrowWhenStartDateIsNull() {
	// 	BusinessLogicException ex = assertThrows(
	// 			BusinessLogicException.class,
	// 			() -> assignmentService.validateAssignmentRequest(
	// 					5L,
	// 					4L,
	// 					5L,
	// 					null
	// 			)
	// 	);
	//
	// 	assertEquals("Дата назначения не может быть null", ex.getMessage());
	// }
	//
	// @Test
	// void validateAssignmentRequest_shouldThrowWhenEmployeeAlreadyHasActiveAssignment() {
	// 	BusinessLogicException ex = assertThrows(
	// 			BusinessLogicException.class,
	// 			() -> assignmentService.validateAssignmentRequest(
	// 					1L,
	// 					4L,
	// 					5L,
	// 					LocalDate.of(2025, 1, 10)
	// 			)
	// 	);
	//
	// 	assertEquals("У сотрудника уже есть активное назначение", ex.getMessage());
	// }
	//
	// @Test
	// void validateAssignmentRequest_shouldThrowWhenNoFreeSlots() {
	// 	BusinessLogicException ex = assertThrows(
	// 			BusinessLogicException.class,
	// 			() -> assignmentService.validateAssignmentRequest(
	// 					5L,
	// 					1L,
	// 					1L,
	// 					LocalDate.of(2025, 1, 10)
	// 			)
	// 	);
	//
	// 	assertEquals("Нет свободных ставок для departmentId=1, positionId=1", ex.getMessage());
	// }
}

