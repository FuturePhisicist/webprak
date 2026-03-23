package ru.msu.cmc.webprak.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "positions", schema = "hr")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"departmentPositions", "assignments"})
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "responsibilities", nullable = false)
    private String responsibilities;

    @OneToMany(mappedBy = "position")
    private List<DepartmentPosition> departmentPositions = new ArrayList<>();

    @OneToMany(mappedBy = "position")
    private List<Assignment> assignments = new ArrayList<>();
}

