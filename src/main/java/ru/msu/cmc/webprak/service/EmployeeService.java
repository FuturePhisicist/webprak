package ru.msu.cmc.webprak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.model.Assignment;
import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.model.EmployeeStatus;
import ru.msu.cmc.webprak.repository.AssignmentRepository;
import ru.msu.cmc.webprak.repository.EmployeeRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AssignmentRepository assignmentRepository;

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public List<Employee> findAllActive() {
        return employeeRepository.findByStatus(EmployeeStatus.ACTIVE);
    }

    public List<Employee> searchByFullName(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        return employeeRepository.searchByFullName(query.trim());
    }

    public Optional<Assignment> findCurrentAssignment(Long employeeId) {
        return assignmentRepository.findByEmployeeIdAndEndDateIsNull(employeeId);
    }

    public List<Assignment> getAssignmentHistory(Long employeeId) {
        return assignmentRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);
    }

    public List<Employee> findActiveEmployeesByDepartment(Long departmentId) {
        return assignmentRepository.findActiveByDepartment(departmentId)
                .stream()
                .map(Assignment::getEmployee)
                .filter(employee -> employee.getStatus() == EmployeeStatus.ACTIVE)
                .toList();
    }

    public List<Employee> findActiveEmployeesByPosition(Long positionId) {
        return assignmentRepository.findActiveByPosition(positionId)
                .stream()
                .map(Assignment::getEmployee)
                .filter(employee -> employee.getStatus() == EmployeeStatus.ACTIVE)
                .toList();
    }
}

