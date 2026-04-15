package DBMS.UniSlot.Backend.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ============================================================
 * AuditLog Entity  (maps to table: audit_logs)
 * ============================================================
 * Immutable log of every significant write operation performed
 * by any user in the system.
 *
 * Every time an admin:
 *  - Creates/updates a department, degree, course, professor, slot
 *  - Marks a student's fee as paid
 *  - Opens or closes a selection window
 *  ... an AuditLog row is created automatically by the service layer.
 *
 * This table is append-only: rows are NEVER updated or deleted.
 * It provides a full change history for compliance and debugging.
 *
 * old_value and new_value store JSON snapshots of the changed object.
 * ============================================================
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    /**
     * The user who performed the action.
     * Could be admin, finance user, or (rarely) a student.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", nullable = false)
    private User actor;

    /**
     * Description of the action.
     * E.g., "FEE_MARKED_PAID", "SLOT_CREATED", "STUDENT_REGISTERED".
     */
    @Column(name = "action", nullable = false, length = 80)
    private String action;

    /**
     * The type of entity that was changed.
     * E.g., "Student", "LectureSlot", "PaymentInvoice".
     */
    @Column(name = "entity_type", nullable = false, length = 80)
    private String entityType;

    /**
     * The database ID of the changed entity.
     * Combined with entityType, uniquely identifies the record.
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * JSON snapshot of the record BEFORE the change.
     * Null for CREATE operations.
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /**
     * JSON snapshot of the record AFTER the change.
     * Null for DELETE operations.
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /**
     * Exact timestamp of the action.
     * Separate from BaseEntity.createdAt to make it explicit
     * that this timestamp is the event time, not the row insert time.
     */
    @Column(name = "acted_at", nullable = false)
    @Builder.Default
    private LocalDateTime actedAt = LocalDateTime.now();
}

