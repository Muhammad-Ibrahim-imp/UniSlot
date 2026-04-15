package DBMS.UniSlot.Backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * DegreeYearSection Entity  (maps to table: degree_year_sections)
 * ============================================================
 * A class section within a specific degree year.
 * E.g., BSCS Year 2 → Section A (max 50 students),
 *                    → Section B (max 50 students).
 *
 * In the old design, sections were not tracked at all — every
 * student just belonged to a department. Now each student is
 * placed in a specific section, giving admin the ability to
 * manage class sizes and diversity across sections.
 * ============================================================
 */
@Entity
@Table(name = "degree_year_sections",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"degree_year_id", "section_name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DegreeYearSection extends BaseEntity {

    /**
     * The year group this section belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_year_id", nullable = false)
    private DegreeYear degreeYear;

    /**
     * Section identifier — typically a single letter (A, B, C).
     */
    @Column(name = "section_name", nullable = false, length = 10)
    private String sectionName;

    /**
     * Maximum number of students allowed in this section.
     */
    @Column(name = "max_students", nullable = false)
    private Integer maxStudents;

    /**
     * Students placed in this section.
     */
    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Student> students = new ArrayList<>();
}
