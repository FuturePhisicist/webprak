package ru.msu.cmc.webprak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.model.Assignment;
import ru.msu.cmc.webprak.model.EducationLevel;
import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.model.EmployeeStatus;
import ru.msu.cmc.webprak.repository.AssignmentRepository;
import ru.msu.cmc.webprak.repository.EmployeeRepository;
import ru.msu.cmc.webprak.service.exception.BusinessLogicException;
import ru.msu.cmc.webprak.service.exception.EntityNotFoundException;

import java.time.LocalDate;
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

    public Optional<Assignment> findCurrentAssignmentWithDetails(Long employeeId) {
        return assignmentRepository.findByEmployeeIdAndEndDateIsNullWithDetails(employeeId);
    }

    public List<Assignment> getAssignmentHistory(Long employeeId) {
        return assignmentRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);
    }

    public List<Assignment> getAssignmentHistoryWithDetails(Long employeeId) {
        return assignmentRepository.findByEmployeeIdOrderByStartDateDescWithDetails(employeeId);
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

    @Transactional
    public Employee create(String lastName,
                           String firstName,
                           String middleName,
                           String homeAddress,
                           EducationLevel education,
                           LocalDate hireDate,
                           EmployeeStatus status) {
        validate(lastName, firstName, homeAddress, education, hireDate, status);

        Employee employee = new Employee();
        apply(employee, lastName, firstName, middleName, homeAddress, education, hireDate, status);
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(Long id,
                           String lastName,
                           String firstName,
                           String middleName,
                           String homeAddress,
                           EducationLevel education,
                           LocalDate hireDate,
                           EmployeeStatus status) {
        validate(lastName, firstName, homeAddress, education, hireDate, status);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден: id=" + id));
        apply(employee, lastName, firstName, middleName, homeAddress, education, hireDate, status);
        return employeeRepository.save(employee);
    }

    @Transactional
    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден: id=" + id));
        if (assignmentRepository.findByEmployeeIdAndEndDateIsNull(id).isPresent()) {
            throw new BusinessLogicException("Нельзя удалить сотрудника с активным назначением");
        }
        employeeRepository.delete(employee);
    }

    private void validate(String lastName,
                          String firstName,
                          String homeAddress,
                          EducationLevel education,
                          LocalDate hireDate,
                          EmployeeStatus status) {
        if (lastName == null || lastName.isBlank()) {
            throw new BusinessLogicException("Фамилия обязательна");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new BusinessLogicException("Имя обязательно");
        }
        if (homeAddress == null || homeAddress.isBlank()) {
            throw new BusinessLogicException("Домашний адрес обязателен");
        }
        if (education == null) {
            throw new BusinessLogicException("Образование обязательно");
        }
        if (hireDate == null) {
            throw new BusinessLogicException("Дата приема обязательна");
        }
        if (status == null) {
            throw new BusinessLogicException("Статус обязателен");
        }
    }

    private void apply(Employee employee,
                       String lastName,
                       String firstName,
                       String middleName,
                       String homeAddress,
                       EducationLevel education,
                       LocalDate hireDate,
                       EmployeeStatus status) {
        employee.setLastName(lastName.trim());
        employee.setFirstName(firstName.trim());
        employee.setMiddleName(middleName == null || middleName.isBlank() ? null : middleName.trim());
        employee.setHomeAddress(homeAddress.trim());
        employee.setEducation(education);
        employee.setHireDate(hireDate);
        employee.setStatus(status);
    }
}
