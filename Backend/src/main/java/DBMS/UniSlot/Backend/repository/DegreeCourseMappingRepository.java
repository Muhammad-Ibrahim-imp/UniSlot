package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.DegreeCourseMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/** DegreeCourseMappingRepository - bridge table between Degree and Course */
@Repository
public interface DegreeCourseMappingRepository extends JpaRepository<DegreeCourseMapping, Long> {
    List<DegreeCourseMapping> findByDegreeId(Long degreeId);
    List<DegreeCourseMapping> findByDegreeIdAndSemesterNumber(Long degreeId, Integer semesterNumber);
    boolean existsByDegreeIdAndCourseIdAndSemesterNumber(Long degreeId, Long courseId, Integer semesterNumber);
    Optional<DegreeCourseMapping> findByDegreeIdAndCourseId(Long degreeId, Long courseId);
    List<DegreeCourseMapping> findByCourseId(Long courseId);
}