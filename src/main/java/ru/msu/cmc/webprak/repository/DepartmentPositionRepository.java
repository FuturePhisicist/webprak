package ru.msu.cmc.webprak.repository;

import ru.msu.cmc.webprak.model.DepartmentPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentPositionRepository extends JpaRepository<DepartmentPosition, Long> {

    Optional<DepartmentPosition> findByDepartmentIdAndPositionId(Long departmentId, Long positionId);

    List<DepartmentPosition> findByDepartmentId(Long departmentId);

    long countByPositionId(Long positionId);
}

