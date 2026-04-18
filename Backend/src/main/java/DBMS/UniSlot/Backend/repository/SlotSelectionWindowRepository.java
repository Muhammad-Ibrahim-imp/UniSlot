package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.SlotSelectionWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SlotSelectionWindowRepository
 * Admin-controlled windows controlling when students can pick slots.
 */
@Repository
public interface SlotSelectionWindowRepository extends JpaRepository<SlotSelectionWindow, Long> {

    List<SlotSelectionWindow> findByDegreeYearId(Long degreeYearId);

    /**
     * Find the currently active window for a degree year.
     * Used by SlotEnrollmentService to gate slot selection.
     */
    @Query("SELECT w FROM SlotSelectionWindow w " +
            "WHERE w.degreeYear.id = :yearId " +
            "AND w.active = true " +
            "AND w.opensAt <= :now " +
            "AND w.closesAt >= :now")
    Optional<SlotSelectionWindow> findActiveWindow(
            @Param("yearId") Long yearId,
            @Param("now") LocalDateTime now);
}
