package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.DegreeYearCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * DegreeYearCourseRepository
 * Bridge table: which courses are offered in which degree year.
 */
@Repository
public interface DegreeYearCourseRepository extends JpaRepository<DegreeYearCourse, Long> {

    /** All course mappings for a degree year. */
    List<DegreeYearCourse> findByDegreeYearId(Long degreeYearId);

    /** Only compulsory courses for a year group. */
    List<DegreeYearCourse> findByDegreeYearIdAndIsCompulsoryTrue(Long degreeYearId);

    boolean existsByDegreeYearIdAndCourseId(Long degreeYearId, Long courseId);

    /** All year groups that include a given course (for cross-dept queries). */
    @Query("SELECT dyc FROM DegreeYearCourse dyc " +
            "JOIN FETCH dyc.degreeYear dy " +
            "JOIN FETCH dy.degreeProgram " +
            "WHERE dyc.course.id = :courseId")
    List<DegreeYearCourse> findByCoursIdWithYearDetails(@Param("courseId") Long courseId);
}