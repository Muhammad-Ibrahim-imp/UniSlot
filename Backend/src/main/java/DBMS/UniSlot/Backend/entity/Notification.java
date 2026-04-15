package DBMS.UniSlot.Backend.entity;


import DBMS.UniSlot.Backend.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

/**
 * ============================================================
 * Notification Entity  (maps to table: notifications)
 * ============================================================
 * System-generated alert delivered to a specific user.
 *
 * Examples:
 *  - FEE_DUE:   "Your semester fee of PKR 45,000 is due by 20 Jan."
 *  - SLOT_OPEN: "Slot selection for BSCS Year 2 opens now."
 *  - SLOT_FULL: "Slot CS301-ALI-AB12 is now full."
 *  - PDF_READY: "Your timetable PDF for Semester 4 is ready."
 *
 * Notifications are created by service methods whenever a
 * relevant event occurs. The student dashboard polls the
 * unread count and displays a notification bell.
 * ============================================================
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    /**
     * The user who should receive this notification.
     * Can be an admin, student, or finance user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Short title shown in the notification bell list.
     * E.g., "Slot selection is open".
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Full notification message body.
     * May contain markdown-like formatting for the frontend.
     */
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    /**
     * Category used by the frontend to render the correct icon
     * and decide what action to show (e.g., "Pay now" button).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    /**
     * False until the user opens/dismisses the notification.
     * Used to compute the unread badge count.
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;
}

