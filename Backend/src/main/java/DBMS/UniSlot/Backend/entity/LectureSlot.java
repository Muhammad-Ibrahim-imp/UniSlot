package DBMS.UniSlot.Backend.entity;



import DBMS.UniSlot.Backend.enums.LectureDay;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * LectureSlot Entity
 * ============================================================
 * The CENTRAL entity of the entire system.
 *
 * A LectureSlot represents a specific offering of a course:
 *   - by a specific Professor
 *   - on specific Days of the week
 *   - at a specific time
 *   - with a maximum capacity (seats)
 *   - with a current enrollment count
 *
 * Example:
 *   Course:    Database Systems (CS301)
 *   Professor: Dr. Ali
 *   Days:      Monday, Wednesday
 *   Time:      08:00 → 09:30
 *   Capacity:  40 students
 *   Enrolled:  17 students
 *
 * Students select a LectureSlot (not just a course) to lock in
 * both the professor AND the time that suits them.
 *
 * ANALYTICS: The system records when this slot was created
 * (slotOpenedAt) and when it first became full (slotFilledAt)
 * to compute how fast this professor fills seats.
 * ============================================================
 */
@Entity
@Table(name = "lecture_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureSlot extends BaseEntity {

    // ── Course & Professor ────────────────────────────────────

    /**
     * The course this slot is an offering of.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * The professor teaching this slot.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    /**
     * The degree year group this slot is scoped to
     * (e.g., "BSCS Year 2"). Students outside this year
     * group cannot enroll in this slot.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_year_id", nullable = false)
    private DegreeYear degreeYear;

    // ── Schedule ──────────────────────────────────────────────

    /**
     * Day of the week this lecture runs.
     * Stored as a string (e.g., "MONDAY") via @Enumerated.
     *
     * NOTE: A single slot can span multiple days per week
     * (e.g., Mon + Wed). We store ONE record per day so that
     * the weekly timetable PDF can be built row by row.
     * The slotGroupCode ties multi-day slots together.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private LectureDay dayOfWeek;

    /**
     * Lecture start time (e.g., 08:00).
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /**
     * Lecture end time (e.g., 09:30).
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Room or location code (e.g., "LH-3", "CS-Lab-2").
     */
    @Column(name = "venue", length = 100)
    private String venue;

    /**
     * A group code linking all days of the same weekly slot.
     * E.g., a Mon+Wed DB slot has both rows sharing
     * slotGroupCode = "CS301-ALI-A".
     * This is how the PDF builder groups multi-day slots.
     */
    @Column(name = "slot_group_code", nullable = false, length = 50)
    private String slotGroupCode;

    // ── Capacity & Enrollment ─────────────────────────────────

    /**
     * Maximum number of students allowed in this slot.
     * Set by the admin when creating the slot.
     */
    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    /**
     * How many students have currently selected this slot.
     * Must never exceed maxCapacity.
     */
    @Column(name = "enrolled_count", nullable = false)
    @Builder.Default
    private Integer enrolledCount = 0;

    // ── Analytics timestamps ──────────────────────────────────

    /**
     * When the admin opened this slot for selection.
     * Used in fill-time calculations.
     */
    @Column(name = "slot_opened_at")
    private LocalDateTime slotOpenedAt;

    /**
     * Automatically set when enrolledCount first hits maxCapacity.
     * (slotFilledAt - slotOpenedAt) = time to fill this slot.
     * Shorter fill time → more popular professor.
     */
    @Column(name = "slot_filled_at")
    private LocalDateTime slotFilledAt;

    // ── Relationships ─────────────────────────────────────────

    /**
     * All student enrollments for this slot.
     */
    @OneToMany(mappedBy = "lectureSlot",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<SlotEnrollment> enrollments = new ArrayList<>();

    // ── Helper Methods ────────────────────────────────────────

    /**
     * @return true if this slot still has seats available.
     */
    public boolean hasAvailableSeats() {
        return enrolledCount < maxCapacity;
    }

    /**
     * Atomically increment enrollment count.
     * Call this inside a @Transactional service method.
     * Also marks slotFilledAt when the last seat is taken.
     */
    public void incrementEnrollment() {
        if (!hasAvailableSeats()) {
            throw new IllegalStateException(
                    "Slot " + slotGroupCode + " on " + dayOfWeek + " is already full.");
        }
        this.enrolledCount++;
        // Mark exactly when the slot became full
        if (this.enrolledCount >= this.maxCapacity && this.slotFilledAt == null) {
            this.slotFilledAt = LocalDateTime.now();
        }
    }

    /**
     * Decrement enrollment count (when a student drops a slot).
     * Also clears slotFilledAt if the slot is no longer full.
     */
    public void decrementEnrollment() {
        if (this.enrolledCount > 0) {
            this.enrolledCount--;
            // If slot is no longer full, clear the filled timestamp
            if (this.enrolledCount < this.maxCapacity) {
                this.slotFilledAt = null;
            }
        }
    }
}

