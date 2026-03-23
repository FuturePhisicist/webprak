package ru.msu.cmc.webprak.repository;

import ru.msu.cmc.webprak.model.Employee;
import ru.msu.cmc.webprak.model.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByStatus(EmployeeStatus status);

    @Query("""
            select e
            from Employee e
            where lower(
                concat(
                    e.lastName, ' ',
                    e.firstName, ' ',
                    coalesce(e.middleName, '')
                )
            ) like lower(concat('%', :query, '%'))
            """)
    List<Employee> searchByFullName(@Param("query") String query);
}

