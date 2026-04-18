package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.DegreeYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * DegreeYearRepository
 * Queries for degree year groups (Year 1, 2, 3, 4 of each program).
 */
@Repository
public interface DegreeYearRepository extends JpaRepository<DegreeYear, Long> {

    /** All year groups for a given degree program. */
    List<DegreeYear> findByDegreeProgramIdOrderByYearNumber(Long degreeProgramId);

    /** Find specific year group (e.g., BSCS Year 2). */
    Optional<DegreeYear> findByDegreeProgramIdAndYearNumber(Long degreeProgramId, Integer yearNumber);

    boolean existsByDegreeProgramIdAndYearNumber(Long degreeProgramId, Integer yearNumber);
}