package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AuditLogRepository
 * Append-only change history for all admin actions.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** Full audit trail for a specific entity (e.g., all changes to Student id=5). */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByActedAtDesc(
            String entityType, Long entityId);

    /** All actions performed by a specific admin user. */
    List<AuditLog> findByActorIdOrderByActedAtDesc(Long actorId);

    /** Actions within a time range — for compliance export. */
    List<AuditLog> findByActedAtBetweenOrderByActedAtDesc(
            LocalDateTime from, LocalDateTime to);

    /** All changes of a given action type (e.g., "FEE_MARKED_PAID"). */
    List<AuditLog> findByActionOrderByActedAtDesc(String action);
}
