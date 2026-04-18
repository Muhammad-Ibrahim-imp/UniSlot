package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.TimetablePdf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * TimetablePdfRepository
 * Audit trail of generated PDF timetables.
 */
@Repository
public interface TimetablePdfRepository extends JpaRepository<TimetablePdf, Long> {

    /** All PDFs ever generated for a student (newest first). */
    List<TimetablePdf> findByStudentIdOrderByGeneratedAtDesc(Long studentId);

    /** The most recent PDF for a student — served as download. */
    Optional<TimetablePdf> findFirstByStudentIdOrderByGeneratedAtDesc(Long studentId);
}
