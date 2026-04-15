package DBMS.UniSlot.Backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Degree Entity
 * ============================================================
 * A degree program under a department (e.g., "BS Computer Science",
 * "BS Software Engineering"). A degree spans multiple years
 * (semesters), and each year/semester has its own set of courses.
 *
 * One Degree → belongs to one Department.
 * One Degree → many DegreeCourseMappings (courses per semester).
 * One Degree → many Students enrolled in it.
 * ============================================================
 */
@Entity
@Table(name = "degrees",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "department_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Degree extends BaseEntity {

    /**
     * Name of the degree (e.g., "BS Computer Science").
     */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /**
     * Short identifier code (e.g., "BSCS", "BSSE").
     */
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    /**
     * Total duration of the degree in years (typically 4).
     * Used to validate semester numbers when linking courses.
     */
    @Column(name = "duration_years", nullable = false)
    private Integer durationYears;

    // ── Relationships ─────────────────────────────────────────

    /**
     * The department that owns this degree.
     * Many degrees can belong to one department.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /**
     * All course-to-semester mappings for this degree.
     * E.g., "Physics is offered to BSCS in semester 4".
     * This is the join table entity between Degree and Course.
     */
    /**
     * Year groups for this degree program (Year 1, 2, 3, 4).
     * Each year group has its own sections, courses, and slots.
     */
    @OneToMany(mappedBy = "degreeProgram",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<DegreeYear> degreeYears = new ArrayList<>();

    /**
     * Fee structures defined for each year of this program.
     */
    @OneToMany(mappedBy = "degreeProgram",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<SemesterFeeStructure> feeStructures = new ArrayList<>();

    /**
     * Students enrolled in this degree program.
     */
    @OneToMany(mappedBy = "degree", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Student> students = new ArrayList<>();
}

