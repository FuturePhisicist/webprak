package ru.msu.cmc.webprak.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.cmc.webprak.BaseIntegrationTest;
import ru.msu.cmc.webprak.model.DepartmentPosition;
import ru.msu.cmc.webprak.model.Position;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PositionServiceTest extends BaseIntegrationTest {

    @Autowired
    private PositionService positionService;

    @Test
    void countDepartmentsUsingPosition_shouldReturnCorrectCount() {
        long count = positionService.countDepartmentsUsingPosition(3L);

        assertEquals(2, count);
    }

    @Test
    void countActiveAssignments_shouldReturnOnlyActiveAssignments() {
        long count = positionService.countActiveAssignments(3L);

        assertEquals(1, count);
    }

    @Test
    void findDepartmentPositions_shouldReturnDepartmentStaffingEntries() {
        List<DepartmentPosition> positions = positionService.findDepartmentPositions(5L);

        assertEquals(2, positions.size());
        assertTrue(positions.stream().anyMatch(dp -> dp.getPosition().getName().equals("Backend-разработчик")));
        assertTrue(positions.stream().anyMatch(dp -> dp.getPosition().getName().equals("QA-инженер")));
    }

    @Test
    void findById_shouldReturnPositionWhenExists() {
        Optional<Position> positionOpt = positionService.findById(4L);

        assertTrue(positionOpt.isPresent());
        assertEquals("QA-инженер", positionOpt.get().getName());
    }

    @Test
    void findById_shouldReturnEmptyWhenPositionDoesNotExist() {
        Optional<Position> positionOpt = positionService.findById(999L);

        assertTrue(positionOpt.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllPositions() {
        List<Position> positions = positionService.findAll();

        assertEquals(5, positions.size());
    }
}

