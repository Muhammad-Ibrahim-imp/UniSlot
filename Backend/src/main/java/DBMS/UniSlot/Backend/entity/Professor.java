package DBMS.UniSlot.Backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Professor Entity
 * ============================================================
 * Represents a university faculty member who teaches courses.
 *
 * KEY FEATURE — Slot Fill Analytics:
 *   The system tracks how quickly each professor's slots fill up.
 *   This data is used by the evaluation committee:
 *   professors whose slots fill fastest are highlighted
 *   for promotion/performance review.
 *
 * One Professor → teaches many LectureSlots (across different courses).
 * ============================================================
 */
@Entity
@Table(name = "professors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Professor extends BaseEntity {

    /**
     * Full name of the professor.
     */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /**
     * Professional email for contact and login reference.
     */
    @Column(name = "email", unique = true, length = 200)
    private String email;

    /**
     * Academic qualifications (e.g., "PhD Computer Science, MIT").
     */
    @Column(name = "qualification", length = 300)
    private String qualification;

    /**
     * Department this professor primarily belongs to.
     * A professor can teach across departments (through LectureSlots)
     * but has one home department.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // ── Analytics Fields ─────────────────────────────────────

    /**
     * Total number of seats offered by this professor across all slots.
     * Incremented when admin creates a new slot for this professor.
     */
    @Column(name = "total_seats_offered", nullable = false)
    @Builder.Default
    private Integer totalSeatsOffered = 0;

    /**
     * Total seats that have been selected by students.
     * Higher fill rate → better evaluation score.
     */
    @Column(name = "total_seats_filled", nullable = false)
    @Builder.Default
    private Integer totalSeatsFilled = 0;

    // ── Relationships ─────────────────────────────────────────

    /**
     * All lecture slots taught by this professor.
     * Cascade: deleting a professor removes all their slots
     * (admin should reassign before deleting).
     */
    @OneToMany(mappedBy = "professor",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<LectureSlot> lectureSlots = new ArrayList<>();

    // ── Helper ───────────────────────────────────────────────

    /**
     * Computed fill rate as a percentage.
     * Used by evaluation reports to rank professors.
     */
    public double getFillRatePercent() {
        if (totalSeatsOffered == 0) return 0.0;
        return (totalSeatsFilled * 100.0) / totalSeatsOffered;
    }
}

