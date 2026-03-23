package ru.msu.cmc.webprak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.model.Assignment;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final DepartmentPositionRepository departmentPositionRepository;

    public Optional<Assignment> findActiveAssignmentByEmployee(Long employeeId) {
        return assignmentRepository.findByEmployeeIdAndEndDateIsNull(employeeId);
    }

    public List<Assignment> findEmployeeHistory(Long employeeId) {
        return assignmentRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);
    }

    public long countOccupiedSlots(Long departmentId, Long positionId) {
        return assignmentRepository.countActiveByDepartmentAndPosition(departmentId, positionId);
    }

    public boolean hasFreeSlots(Long departmentId, Long positionId) {
        DepartmentPosition departmentPosition = departmentPositionRepository
                .findByDepartmentIdAndPositionId(departmentId, positionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Штатная позиция не найдена для departmentId=%d, positionId=%d"
                                .formatted(departmentId, positionId)
                ));

        long occupied = countOccupiedSlots(departmentId, positionId);
        return occupied < departmentPosition.getSlotsTotal();
    }

    @Transactional
    public Assignment assignEmployee(Long employeeId,
                                     Long departmentId,
                                     Long positionId,
                                     LocalDate startDate,
                                     String note) {
        if (startDate == null) {
            throw new BusinessLogicException("Дата назначения не может быть null");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Сотрудник не найден: id=" + employeeId));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Подразделение не найдено: id=" + departmentId));

        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Должность не найдена: id=" + positionId));

        departmentPositionRepository.findByDepartmentIdAndPositionId(departmentId, positionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Для подразделения id=%d не предусмотрена должность id=%d"
                                .formatted(departmentId, positionId)
                ));

        if (assignmentRepository.findByEmployeeIdAndEndDateIsNull(employeeId).isPresent()) {
            throw new BusinessLogicException(
                    "У сотрудника уже есть активное назначение");
        }

        if (!hasFreeSlots(departmentId, positionId)) {
            throw new BusinessLogicException(
                    "Нет свободных ставок для departmentId=%d, positionId=%d"
                            .formatted(departmentId, positionId));
        }

        Assignment assignment = new Assignment();
        assignment.setEmployee(employee);
        assignment.setDepartment(department);
        assignment.setPosition(position);
        assignment.setStartDate(startDate);
        assignment.setEndDate(null);
        assignment.setNote(note);

        return assignmentRepository.save(assignment);
    }

    @Transactional
    public Assignment closeActiveAssignment(Long employeeId, LocalDate endDate) {
        if (endDate == null) {
            throw new BusinessLogicException("Дата завершения не может быть null");
        }

        Assignment activeAssignment = assignmentRepository.findByEmployeeIdAndEndDateIsNull(employeeId)
                .orElseThrow(() -> new BusinessLogicException(
                        "У сотрудника нет активного назначения"));

        if (endDate.isBefore(activeAssignment.getStartDate())) {
            throw new BusinessLogicException(
                    "Дата завершения не может быть раньше даты начала назначения");
        }

        activeAssignment.setEndDate(endDate);
        return assignmentRepository.save(activeAssignment);
    }

    @Transactional
    public Assignment transferEmployee(Long employeeId,
                                       Long departmentId,
                                       Long positionId,
                                       LocalDate transferDate,
                                       String note) {
        if (transferDate == null) {
            throw new BusinessLogicException("Дата перевода не может быть null");
        }

        Assignment currentAssignment = assignmentRepository.findByEmployeeIdAndEndDateIsNull(employeeId)
                .orElseThrow(() -> new BusinessLogicException(
                        "Невозможно перевести сотрудника без активного назначения"));

        LocalDate endDateForCurrentAssignment = transferDate.minusDays(1);

        if (endDateForCurrentAssignment.isBefore(currentAssignment.getStartDate())) {
            throw new BusinessLogicException(
                    "Дата перевода конфликтует с текущим назначением");
        }

        closeActiveAssignment(employeeId, endDateForCurrentAssignment);

        return assignEmployee(employeeId, departmentId, positionId, transferDate, note);
    }
}

