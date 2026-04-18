package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.SemesterFeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SemesterFeeStructureRepository
 * Fee breakdown per degree program per year.
 */
@Repository
public interface SemesterFeeStructureRepository extends JpaRepository<SemesterFeeStructure, Long> {

    /** All fee structures for a degree program (all years). */
    List<SemesterFeeStructure> findByDegreeProgramId(Long degreeProgramId);

    /**
     * Find the currently active fee structure for a specific
     * degree program, year, and semester.
     * "Active" = validFrom <= now <= validTo (or validTo is null).
     */
    @Query("SELECT f FROM SemesterFeeStructure f " +
            "WHERE f.degreeProgram.id = :progId " +
            "AND f.yearNumber = :year " +
            "AND f.semesterNumber = :semester " +
            "AND f.validFrom <= :now " +
            "AND (f.validTo IS NULL OR f.validTo >= :now)")
    Optional<SemesterFeeStructure> findActive(
            @Param("progId") Long progId,
            @Param("year") Integer year,
            @Param("semester") Integer semester,
            @Param("now") LocalDateTime now);
}
