package ru.msu.cmc.webprak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.model.DepartmentPosition;
import ru.msu.cmc.webprak.model.Position;
import ru.msu.cmc.webprak.repository.AssignmentRepository;
import ru.msu.cmc.webprak.repository.DepartmentPositionRepository;
import ru.msu.cmc.webprak.repository.PositionRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentPositionRepository departmentPositionRepository;
    private final AssignmentRepository assignmentRepository;

    public List<Position> findAll() {
        return positionRepository.findAll();
    }

    public Optional<Position> findById(Long id) {
        return positionRepository.findById(id);
    }

    public long countDepartmentsUsingPosition(Long positionId) {
        return departmentPositionRepository.countByPositionId(positionId);
    }

    public long countActiveAssignments(Long positionId) {
        return assignmentRepository.countActiveByPosition(positionId);
    }

    public List<DepartmentPosition> findDepartmentPositions(Long departmentId) {
        return departmentPositionRepository.findByDepartmentId(departmentId);
    }
}

