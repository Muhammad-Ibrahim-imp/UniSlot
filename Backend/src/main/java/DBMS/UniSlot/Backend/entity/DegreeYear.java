package DBMS.UniSlot.Backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * DegreeYear Entity  (maps to table: degree_years)
 * ============================================================
 * Represents one academic year inside a degree program.
 * Example rows for BSCS:
 *   year_number=1 → "First Year BSCS"
 *   year_number=2 → "Second Year BSCS"
 *   year_number=3 → "Third Year BSCS"
 *   year_number=4 → "Fourth Year BSCS"
 *
 * WHY THIS EXISTS:
 *   In the old design, all degree structure was in one Degree
 *   entity. This made it impossible to scope lecture slots,
 *   selection windows, and course offerings to a specific year
 *   cohort. DegreeYear solves that by giving each year group
 *   its own entity with its own sections, slots, and window.
 *
 * Relationships:
 *   DegreeProgram  ||--o{  DegreeYear          (many years per program)
 *   DegreeYear     ||--o{  DegreeYearSection   (sections A/B/C per year)
 *   DegreeYear     ||--o{  DegreeYearCourse    (course catalogue for year)
 *   DegreeYear     ||--o{  LectureSlot         (slots scoped to this year)
 *   DegreeYear     ||--o{  SlotSelectionWindow (when students can register)
 *   DegreeYear     ||--o{  Student             (students currently in this year)
 * ============================================================
 */
@Entity
@Table(name = "degree_years",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"degree_program_id", "year_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DegreeYear extends BaseEntity {

    /**
     * The parent degree program (e.g., BSCS).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_program_id", nullable = false)
    private Degree degreeProgram;

    /**
     * Which year of the program this represents (1, 2, 3 or 4).
     * Valid range: 1 → degreeProgram.durationYears.
     */
    @Column(name = "year_number", nullable = false)
    private Integer yearNumber;

    /**
     * Human-readable label shown in the UI.
     * E.g., "First Year BSCS", "Second Year BSSE".
     */
    @Column(name = "label", nullable = false, length = 100)
    private String label;

    // ── Relationships ─────────────────────────────────────────

    /**
     * Class sections for this year group (A, B, C ...).
     * Cascade: sections are deleted when the year group is removed.
     */
    @OneToMany(mappedBy = "degreeYear",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<DegreeYearSection> sections = new ArrayList<>();

    /**
     * Course-to-year mappings: which courses are taught in this year.
     */
    @OneToMany(mappedBy = "degreeYear",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<DegreeYearCourse> yearCourses = new ArrayList<>();

    /**
     * Slot selection windows for this year group.
     */
    @OneToMany(mappedBy = "degreeYear",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<SlotSelectionWindow> selectionWindows = new ArrayList<>();

    /**
     * Students currently enrolled in this year group.
     */
    @OneToMany(mappedBy = "degreeYear", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Student> students = new ArrayList<>();
}

