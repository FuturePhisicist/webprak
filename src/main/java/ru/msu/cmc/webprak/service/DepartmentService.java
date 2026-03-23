package ru.msu.cmc.webprak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.model.Department;
import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.repository.AssignmentRepository;
import ru.msu.cmc.webprak.repository.DepartmentRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final AssignmentRepository assignmentRepository;

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Optional<Department> findById(Long id) {
        return departmentRepository.findById(id);
    }

    public List<Department> findRootDepartments() {
        return departmentRepository.findByParentDepartmentIsNull();
    }

    public List<Department> findChildDepartments(Long departmentId) {
        return departmentRepository.findByParentDepartmentId(departmentId);
    }

    public long countChildDepartments(Long departmentId) {
        return departmentRepository.findByParentDepartmentId(departmentId).size();
    }

    public long countActiveEmployees(Long departmentId) {
        return assignmentRepository.countActiveByDepartment(departmentId);
    }

    public Optional<Employee> findManager(Long departmentId) {
        return departmentRepository.findManagerByDepartmentId(departmentId);
    }
}

