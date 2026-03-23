package ru.msu.cmc.webprak.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "employees", schema = "hr")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"assignments", "managedDepartments"})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long id;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "home_address", nullable = false)
    private String homeAddress;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "education", nullable = false, columnDefinition = "education_level")
    private EducationLevel education;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "employee_status")
    private EmployeeStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "employee")
    private List<Assignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "manager")
    private List<Department> managedDepartments = new ArrayList<>();

    public String getFullName() {
        return middleName == null || middleName.isBlank()
                ? lastName + " " + firstName
                : lastName + " " + firstName + " " + middleName;
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = EmployeeStatus.ACTIVE;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}

