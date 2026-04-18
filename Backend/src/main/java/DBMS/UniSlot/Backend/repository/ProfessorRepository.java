package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProfessorRepository
 *
 * Supports CRUD and evaluation-related analytics queries.
 * The evaluation queries return professors ordered by how
 * quickly their slots fill — used by the promotion committee.
 */
@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    Optional<Professor> findByEmail(String email);

    boolean existsByEmail(String email);

    /** All professors belonging to a department. */
    List<Professor> findByDepartmentId(Long departmentId);

    /**
     * EVALUATION QUERY — Professors ranked by fill rate (desc).
     * Fill rate = totalSeatsFilled / totalSeatsOffered.
     *
     * Professors whose slots fill fastest score highest.
     * Used by the admin evaluation dashboard.
     *
     * NULLIF prevents division-by-zero when a professor has
     * no slots yet (returns null → treated as 0 by COALESCE).
     */
    @Query("SELECT p FROM Professor p " +
            "WHERE p.totalSeatsOffered > 0 " +
            "ORDER BY (CAST(p.totalSeatsFilled AS Double) / p.totalSeatsOffered) DESC")
    List<Professor> findAllOrderedByFillRateDesc();

    /**
     * EVALUATION QUERY — Professors with at least one fully
     * filled slot. Used for the "Star Professors" leaderboard.
     */
    @Query("SELECT DISTINCT p FROM Professor p " +
            "JOIN p.lectureSlots ls " +
            "WHERE ls.slotFilledAt IS NOT NULL")
    List<Professor> findProfessorsWithFilledSlots();
}
