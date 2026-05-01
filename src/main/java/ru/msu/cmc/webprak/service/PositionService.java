package ru.msu.cmc.webprak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.model.DepartmentPosition;
import ru.msu.cmc.webprak.model.Position;
import ru.msu.cmc.webprak.repository.AssignmentRepository;
import ru.msu.cmc.webprak.repository.DepartmentPositionRepository;
import ru.msu.cmc.webprak.repository.DepartmentRepository;
import ru.msu.cmc.webprak.repository.PositionRepository;
import ru.msu.cmc.webprak.service.exception.BusinessLogicException;
import ru.msu.cmc.webprak.service.exception.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentPositionRepository departmentPositionRepository;
    private final AssignmentRepository assignmentRepository;
    private final DepartmentRepository departmentRepository;

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

    public List<DepartmentPosition> findDepartmentPositionsByPosition(Long positionId) {
        return departmentPositionRepository.findByPositionIdWithDepartment(positionId);
    }

    @Transactional
    public DepartmentPosition addPositionToDepartment(Long departmentId, Long positionId, Integer slotsTotal) {
        if (slotsTotal == null || slotsTotal <= 0) {
            throw new BusinessLogicException("Количество ставок должно быть положительным");
        }
        if (departmentPositionRepository.findByDepartmentIdAndPositionId(departmentId, positionId).isPresent()) {
            throw new BusinessLogicException("Должность уже предусмотрена в подразделении");
        }

        DepartmentPosition departmentPosition = new DepartmentPosition();
        departmentPosition.setDepartment(departmentRepository.findById(departmentId)
                .orElseThrow(() -> new EntityNotFoundException("Подразделение не найдено: id=" + departmentId)));
        departmentPosition.setPosition(positionRepository.findById(positionId)
                .orElseThrow(() -> new EntityNotFoundException("Должность не найдена: id=" + positionId)));
        departmentPosition.setSlotsTotal(slotsTotal);
        return departmentPositionRepository.save(departmentPosition);
    }

    @Transactional
    public void removePositionFromDepartment(Long departmentId, Long positionId) {
        DepartmentPosition departmentPosition = departmentPositionRepository
                .findByDepartmentIdAndPositionId(departmentId, positionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Для подразделения id=%d не предусмотрена должность id=%d".formatted(departmentId, positionId)));
        if (assignmentRepository.countActiveByDepartmentAndPosition(departmentId, positionId) > 0) {
            throw new BusinessLogicException("Нельзя удалить должность из подразделения: есть активные назначения");
        }
        departmentPositionRepository.delete(departmentPosition);
    }

    @Transactional
    public Position create(String name, String responsibilities) {
        Position position = new Position();
        apply(position, name, responsibilities);
        return positionRepository.save(position);
    }

    @Transactional
    public Position update(Long id, String name, String responsibilities) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Должность не найдена: id=" + id));
        apply(position, name, responsibilities);
        return positionRepository.save(position);
    }

    @Transactional
    public void delete(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Должность не найдена: id=" + id));
        if (assignmentRepository.countActiveByPosition(id) > 0) {
            throw new BusinessLogicException("Нельзя удалить должность с активными назначениями");
        }
        positionRepository.delete(position);
    }

    private void apply(Position position, String name, String responsibilities) {
        if (name == null || name.isBlank()) {
            throw new BusinessLogicException("Название должности обязательно");
        }
        if (responsibilities == null || responsibilities.isBlank()) {
            throw new BusinessLogicException("Описание обязанностей обязательно");
        }
        position.setName(name.trim());
        position.setResponsibilities(responsibilities.trim());
    }
}
