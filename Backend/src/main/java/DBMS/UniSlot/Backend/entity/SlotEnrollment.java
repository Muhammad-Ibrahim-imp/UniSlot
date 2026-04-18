package DBMS.UniSlot.Backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ============================================================
 * SlotEnrollment Entity
 * ============================================================
 * Records the fact that a specific Student selected a specific
 * LectureSlot. This is the many-to-many bridge entity between
 * Student and LectureSlot, enriched with metadata.
 *
 * Why not a plain @ManyToMany?
 *   We need to store enrolledAt (timestamp), and potentially
 *   future fields like grade or attendance. A join entity
 *   is cleaner and more extensible.
 *
 * Business Rules:
 *  - A student can have at most ONE enrollment per course
 *    (enforced via unique constraint on student_id + course).
 *  - The LectureSlot's enrolledCount is incremented when
 *    this record is created (done in SlotEnrollmentService).
 * ============================================================
 */
@Entity
@Table(name = "slot_enrollments",
        uniqueConstraints = @UniqueConstraint(
                // A student cannot select two slots for the same course
                columnNames = {"student_id", "slot_group_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotEnrollment extends BaseEntity {

    /**
     * The student who made the selection.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * The specific lecture slot the student chose.
     * (This is ONE of potentially many day-entries for a weekly slot.)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lecture_slot_id", nullable = false)
    private LectureSlot lectureSlot;

    /**
     * The group code copied from LectureSlot to quickly identify
     * which weekly slot group this enrollment belongs to.
     * Used in the unique constraint to prevent double-enrollment
     * in the same slot group.
     */
    @Column(name = "slot_group_code", nullable = false, length = 50)
    private String slotGroupCode;

    /**
     * Exact timestamp when the student confirmed their selection.
     * Useful for audit trails and resolving disputes.
     */
    @Column(name = "enrolled_at", nullable = false)
    @Builder.Default
    private LocalDateTime enrolledAt = LocalDateTime.now();

    /**
     * Whether the student has dropped this slot.
     * Soft-delete: we keep the record so the LectureSlot's
     * decrementEnrollment() can be called and the history is preserved.
     */
    @Column(name = "is_dropped", nullable = false)
    @Builder.Default
    private boolean dropped = false;

    /**
     * Timestamp when the slot was dropped (null if still enrolled).
     */
    @Column(name = "dropped_at")
    private LocalDateTime droppedAt;
}

