package ru.msu.cmc.webprak.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.msu.cmc.webprak.model.Assignment;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    Optional<Assignment> findByEmployeeIdAndEndDateIsNull(Long employeeId);

    List<Assignment> findByEmployeeIdOrderByStartDateDesc(Long employeeId);

    @Query("""
            select count(a)
            from Assignment a
            where a.department.id = :departmentId
              and a.position.id = :positionId
              and a.endDate is null
            """)
    long countActiveByDepartmentAndPosition(@Param("departmentId") Long departmentId,
                                            @Param("positionId") Long positionId);

    @Query("""
            select count(a)
            from Assignment a
            where a.department.id = :departmentId
              and a.endDate is null
            """)
    long countActiveByDepartment(@Param("departmentId") Long departmentId);

    @Query("""
            select count(a)
            from Assignment a
            where a.position.id = :positionId
              and a.endDate is null
            """)
    long countActiveByPosition(@Param("positionId") Long positionId);

    @Query("""
            select a
            from Assignment a
            join fetch a.employee
            where a.department.id = :departmentId
              and a.endDate is null
            """)
    List<Assignment> findActiveByDepartment(@Param("departmentId") Long departmentId);

    @Query("""
            select a
            from Assignment a
            join fetch a.employee
            where a.position.id = :positionId
              and a.endDate is null
            """)
    List<Assignment> findActiveByPosition(@Param("positionId") Long positionId);
}

