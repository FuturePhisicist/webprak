package ru.msu.cmc.webprak.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.msu.cmc.webprak.model.DepartmentPosition;

import java.util.List;
import java.util.Optional;

public interface DepartmentPositionRepository extends JpaRepository<DepartmentPosition, Long> {

    Optional<DepartmentPosition> findByDepartmentIdAndPositionId(Long departmentId, Long positionId);

    @Query("""
            select dp
            from DepartmentPosition dp
            join fetch dp.position
            where dp.department.id = :departmentId
            """)
    List<DepartmentPosition> findByDepartmentId(Long departmentId);

    long countByPositionId(Long positionId);
}

