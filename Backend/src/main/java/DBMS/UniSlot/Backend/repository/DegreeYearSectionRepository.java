package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.DegreeYearSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * DegreeYearSectionRepository
 * Sections (A, B, C) within a degree year group.
 */
@Repository
public interface DegreeYearSectionRepository extends JpaRepository<DegreeYearSection, Long> {

    List<DegreeYearSection> findByDegreeYearId(Long degreeYearId);

    Optional<DegreeYearSection> findByDegreeYearIdAndSectionName(Long degreeYearId, String sectionName);

    boolean existsByDegreeYearIdAndSectionName(Long degreeYearId, String sectionName);
}