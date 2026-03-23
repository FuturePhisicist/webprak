package ru.msu.cmc.webprak.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.msu.cmc.webprak.model.Department;
import ru.msu.cmc.webprak.model.Employee;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByParentDepartmentIsNull();

    List<Department> findByParentDepartmentId(Long parentDepartmentId);

    @Query("""
            select d.manager
            from Department d
            where d.id = :departmentId
            """)
    Optional<Employee> findManagerByDepartmentId(Long departmentId);
}

