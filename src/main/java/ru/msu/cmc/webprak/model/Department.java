package ru.msu.cmc.webprak.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments", schema = "hr")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"parentDepartment", "childDepartments", "manager", "departmentPositions", "assignments"})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;

    @OneToMany(mappedBy = "parentDepartment")
    private List<Department> childDepartments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_employee_id")
    private Employee manager;

    @OneToMany(mappedBy = "department")
    private List<DepartmentPosition> departmentPositions = new ArrayList<>();

    @OneToMany(mappedBy = "department")
    private List<Assignment> assignments = new ArrayList<>();
}

