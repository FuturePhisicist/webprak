package ru.msu.cmc.webprak.repository;

import ru.msu.cmc.webprak.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByParentDepartmentIsNull();

    List<Department> findByParentDepartmentId(Long parentDepartmentId);
}

