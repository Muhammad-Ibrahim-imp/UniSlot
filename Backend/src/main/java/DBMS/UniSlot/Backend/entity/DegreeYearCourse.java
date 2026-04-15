package DBMS.UniSlot.Backend.entity;


import jakarta.persistence.*;
import lombok.*;

/**
 * ============================================================
 * DegreeYearCourse Entity  (maps to table: degree_year_courses)
 * ============================================================
 * Bridge table linking a Course to a specific DegreeYear.
 * Replaces the old DegreeCourseMapping which only linked
 * Course → Degree with a semester number.
 *
 * The new model is more precise:
 *   DegreeYear (e.g., "BSCS Year 2") → Course (CS301)
 *   is_compulsory = true
 *
 * This directly answers "what courses does Year 2 BSCS study?"
 * without requiring a semester-number lookup.
 *
 * The old semester_number is now derived from the year group
 * itself (degreeYear.yearNumber * 2 - 1 or *2 for sem2).
 * ============================================================
 */
@Entity
@Table(name = "degree_year_courses",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"degree_year_id", "course_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DegreeYearCourse extends BaseEntity {

    /**
     * The degree year this course belongs to
     * (e.g., "BSCS Year 2").
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_year_id", nullable = false)
    private DegreeYear degreeYear;

    /**
     * The course being offered in this year
     * (e.g., "Database Systems CS301").
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * Whether this course is compulsory for the year group.
     * Compulsory courses must be selected; electives are optional.
     */
    @Column(name = "is_compulsory", nullable = false)
    @Builder.Default
    private Boolean isCompulsory = true;
}

