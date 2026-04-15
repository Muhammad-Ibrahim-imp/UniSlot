package DBMS.UniSlot.Backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Department Entity
 * ============================================================
 * Represents a university department (e.g., "Computer Science",
 * "Software Engineering", "Electrical Engineering").
 *
 * One Department → many Degrees.
 * One Department → many Students.
 * ============================================================
 */
@Entity
@Table(name = "departments",
        uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseEntity {

    /**
     * Full name of the department (e.g., "Department of Computer Science").
     * Must be unique across the university.
     */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /**
     * Short code used for display and referencing
     * (e.g., "CS", "SE", "EE").
     */
    @Column(name = "code", nullable = false, length = 10, unique = true)
    private String code;

    // ── Relationships ─────────────────────────────────────────

    /**
     * All degree programs offered by this department.
     * CascadeType.ALL → deleting a department also deletes its degrees.
     * orphanRemoval  → degrees not linked to any department are deleted.
     */
    @OneToMany(mappedBy = "department",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Degree> degrees = new ArrayList<>();

    /**
     * Students who belong to this department as their primary department.
     * We do NOT cascade delete here — students must be managed independently.
     */
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Student> students = new ArrayList<>();
}
