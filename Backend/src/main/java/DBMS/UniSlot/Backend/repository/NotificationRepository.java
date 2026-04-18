package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.Notification;
import DBMS.UniSlot.Backend.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * NotificationRepository
 * User notifications (fee due, slot open, PDF ready, etc.)
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** All notifications for a user — shown in the bell dropdown. */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Unread count — shown as the badge number. */
    long countByUserIdAndReadFalse(Long userId);

    /** Unread notifications for a user. */
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    /** Find notifications by type (e.g., all FEE_DUE for broadcast). */
    List<Notification> findByUserIdAndType(Long userId, NotificationType type);
}
