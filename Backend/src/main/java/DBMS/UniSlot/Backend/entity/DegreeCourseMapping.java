package DBMS.UniSlot.Backend.entity;


import jakarta.persistence.*;
import lombok.*;

/**
 * ============================================================
 * DegreeCourseMapping Entity
 * ============================================================
 * This is the BRIDGE TABLE between Degree and Course.
 *
 * Problem it solves:
 *   The same course (e.g., Physics PHY101) appears in:
 *     - BSCS at Semester 4
 *     - BSSE at Semester 2
 *   A simple ManyToMany between Degree and Course cannot
 *   store the semester number, so we use this explicit
 *   join entity.
 *
 * This also means a student in BSCS sees PHY101 listed under
 * semester 4 of their plan, while BSSE students see it under
 * semester 2 — even though it's the same Course record and
 * the same LectureSlots are available to both.
 * ============================================================
 */
@Entity
@Table(name = "degree_course_mappings",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"degree_id", "course_id", "semester_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DegreeCourseMapping extends BaseEntity {

    /**
     * The degree this mapping belongs to.
     * E.g., "BS Computer Science".
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_id", nullable = false)
    private Degree degree;

    /**
     * The course being mapped.
     * E.g., "Physics — PHY101".
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * The semester in which this course is offered for this degree.
     * E.g., semester 4 for BSCS, semester 2 for BSSE.
     * Valid range: 1 → (durationYears * 2).
     */
    @Column(name = "semester_number", nullable = false)
    private Integer semesterNumber;

    /**
     * Whether passing this course is mandatory for graduation.
     * Compulsory courses still can be selected from any department
     * that offers them (same course code).
     */
    @Column(name = "is_compulsory", nullable = false)
    @Builder.Default
    private Boolean isCompulsory = true;
}

