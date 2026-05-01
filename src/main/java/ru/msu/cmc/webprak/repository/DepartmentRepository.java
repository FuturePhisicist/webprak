package ru.msu.cmc.webprak.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.msu.cmc.webprak.model.Department;
import ru.msu.cmc.webprak.model.Employee;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query("""
            select d
            from Department d
            left join fetch d.parentDepartment
            left join fetch d.manager
            order by d.id
            """)
    List<Department> findAllWithDetails();

    @Query("""
            select d
            from Department d
            left join fetch d.parentDepartment
            left join fetch d.manager
            where d.id = :id
            """)
    Optional<Department> findByIdWithDetails(@Param("id") Long id);

    List<Department> findByParentDepartmentIsNull();

    List<Department> findByParentDepartmentId(Long parentDepartmentId);

    @Query("""
            select d.manager
            from Department d
            where d.id = :departmentId
            """)
    Optional<Employee> findManagerByDepartmentId(@Param("departmentId") Long departmentId);
}
