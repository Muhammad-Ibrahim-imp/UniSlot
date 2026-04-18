package DBMS.UniSlot.Backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Course Entity
 * ============================================================
 * A university course identified by a unique course code
 * (e.g., CS301 — Database Systems, PHY201 — Applied Physics).
 *
 * KEY DESIGN DECISION:
 * The same course (same course code) can appear in multiple
 * degrees at different semester numbers.
 * E.g., "Physics (PHY101)" is offered to CS students in
 * semester 4 and to SE students in semester 2.
 * This sharing is handled through DegreeCourseMapping.
 *
 * One Course → many DegreeCourseMapping rows (shared across degrees).
 * One Course → many LectureSlots (the actual timed offerings).
 * ============================================================
 */
@Entity
@Table(name = "courses",
        uniqueConstraints = @UniqueConstraint(columnNames = "course_code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {

    /**
     * Display name of the course (e.g., "Database Systems").
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Globally unique course code (e.g., "CS301", "PHY101").
     * This is the key used to identify the same course
     * across multiple departments/degrees.
     */
    @Column(name = "course_code", nullable = false, length = 20, unique = true)
    private String courseCode;

    /**
     * Academic credit hours (e.g., 3 for a theory course,
     * 1 for a lab). Determines workload and fee calculation.
     */
    @Column(name = "credit_hours", nullable = false)
    private Integer creditHours;

    /**
     * Optional description of what the course covers.
     */
    @Column(name = "description", length = 1000)
    private String description;

    // ── Relationships ─────────────────────────────────────────

    /**
     * All semester-based degree mappings for this course (DegreeCourseMapping).
     * Used by admin to assign the course to a degree at a specific semester number.
     * E.g., PHY101 → BSCS at semester 4, PHY101 → BSSE at semester 2.
     */
    @OneToMany(mappedBy = "course",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<DegreeCourseMapping> degreeMappings = new ArrayList<>();

    /**
     * All degree-year mappings where this course appears.
     * A course can appear in multiple year groups across degrees.
     */
    @OneToMany(mappedBy = "course",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<DegreeYearCourse> degreeYearCourses = new ArrayList<>();

    /**
     * Timed lecture slots that professors offer for this course.
     * E.g., Prof. Ali teaches DB on Mon/Wed 8-9am, Slot Capacity: 40.
     */
    @OneToMany(mappedBy = "course",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<LectureSlot> lectureSlots = new ArrayList<>();
}

