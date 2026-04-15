package DBMS.UniSlot.Backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ============================================================
 * SlotSelectionWindow Entity  (maps to table: slot_selection_windows)
 * ============================================================
 * Admin controls WHEN students of a specific degree year can
 * select their lecture slots. Outside of this window, the
 * slot selection API returns 409 even for paid students.
 *
 * One active window per degree year at any time.
 * Example:
 *   degreeYear: "BSCS Year 2"
 *   label:      "Semester 2024-I Registration"
 *   opens_at:   2024-01-10 09:00
 *   closes_at:  2024-01-20 23:59
 *   is_active:  true
 *
 * The admin can close the window early by setting is_active=false.
 * ============================================================
 */
@Entity
@Table(name = "slot_selection_windows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotSelectionWindow extends BaseEntity {

    /**
     * The degree year this window applies to.
     * E.g., only BSCS Year 2 students can select slots
     * during this window.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_year_id", nullable = false)
    private DegreeYear degreeYear;

    /**
     * Descriptive label shown to students.
     * E.g., "Semester 2024-I Slot Registration".
     */
    @Column(name = "label", nullable = false, length = 100)
    private String label;

    /** Window open timestamp — slot selection is blocked before this. */
    @Column(name = "opens_at", nullable = false)
    private LocalDateTime opensAt;

    /** Window close timestamp — slot selection is blocked after this. */
    @Column(name = "closes_at", nullable = false)
    private LocalDateTime closesAt;

    /**
     * Admin can manually close the window early.
     * If false, slot selection is blocked regardless of timestamps.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    // ── Business helpers ──────────────────────────────────────

    /**
     * @return true if the window is currently accepting slot selections.
     */
    public boolean isCurrentlyOpen() {
        LocalDateTime now = LocalDateTime.now();
        return active
                && now.isAfter(opensAt)
                && now.isBefore(closesAt);
    }
}