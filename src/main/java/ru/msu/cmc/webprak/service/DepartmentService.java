package ru.msu.cmc.webprak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.model.Department;
import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.repository.AssignmentRepository;
import ru.msu.cmc.webprak.repository.DepartmentRepository;
import ru.msu.cmc.webprak.repository.EmployeeRepository;
import ru.msu.cmc.webprak.service.exception.BusinessLogicException;
import ru.msu.cmc.webprak.service.exception.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;

    public List<Department> findAll() {
        return departmentRepository.findAllWithDetails();
    }

    public Optional<Department> findById(Long id) {
        return departmentRepository.findByIdWithDetails(id);
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

    @Transactional
    public Department create(String name, Long parentDepartmentId, Long managerEmployeeId) {
        Department department = new Department();
        apply(department, name, parentDepartmentId, managerEmployeeId);
        return departmentRepository.save(department);
    }

    @Transactional
    public Department update(Long id, String name, Long parentDepartmentId, Long managerEmployeeId) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Подразделение не найдено: id=" + id));
        if (parentDepartmentId != null && parentDepartmentId.equals(id)) {
            throw new BusinessLogicException("Подразделение не может быть головным для самого себя");
        }
        apply(department, name, parentDepartmentId, managerEmployeeId);
        return departmentRepository.save(department);
    }

    @Transactional
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Подразделение не найдено: id=" + id));
        if (!departmentRepository.findByParentDepartmentId(id).isEmpty()) {
            throw new BusinessLogicException("Нельзя удалить подразделение с внутренними подразделениями");
        }
        if (assignmentRepository.countActiveByDepartment(id) > 0) {
            throw new BusinessLogicException("Нельзя удалить подразделение с активными сотрудниками");
        }
        departmentRepository.delete(department);
    }

    private void apply(Department department, String name, Long parentDepartmentId, Long managerEmployeeId) {
        if (name == null || name.isBlank()) {
            throw new BusinessLogicException("Название подразделения обязательно");
        }
        department.setName(name.trim());
        department.setParentDepartment(parentDepartmentId == null
                ? null
                : departmentRepository.findById(parentDepartmentId)
                        .orElseThrow(() -> new EntityNotFoundException("Головное подразделение не найдено: id=" + parentDepartmentId)));
        department.setManager(managerEmployeeId == null
                ? null
                : employeeRepository.findById(managerEmployeeId)
                        .orElseThrow(() -> new EntityNotFoundException("Руководитель не найден: id=" + managerEmployeeId)));
    }
}
