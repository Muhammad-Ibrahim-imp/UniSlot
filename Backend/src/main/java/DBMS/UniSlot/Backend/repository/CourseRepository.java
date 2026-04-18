package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CourseRepository
 *
 * Courses are the central academic unit. Key queries here:
 *  - Find by course code (globally unique identifier).
 *  - Find all courses offered in a specific semester of a degree.
 *  - Find courses offered across multiple departments (shared courses).
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);

    /**
     * Get all courses a student in a specific degree+semester can enroll in.
     * This is the primary query for the "Select My Courses" screen.
     *
     * A student in BSCS semester 4 sees:
     *   - All courses mapped to BSCS at semester 4.
     *   - If PHY101 is also mapped to BSSE at semester 2,
     *     the same course appears but is de-duplicated by course code.
     */
    @Query("SELECT DISTINCT c FROM Course c " +
            "JOIN c.degreeMappings dm " +
            "WHERE dm.degree.id = :degreeId " +
            "AND dm.semesterNumber = :semesterNumber")
    List<Course> findByDegreeAndSemester(
            @Param("degreeId") Long degreeId,
            @Param("semesterNumber") Integer semesterNumber);

    /**
     * Find all courses offered by more than one degree.
     * Used by admin reports to see shared resources.
     */
    @Query("SELECT c FROM Course c " +
            "WHERE SIZE(c.degreeMappings) > 1")
    List<Course> findCoursesSharedAcrossDegrees();

    /**
     * Search courses by name (case-insensitive partial match).
     * Useful for the course search bar in the student UI.
     */
    List<Course> findByNameContainingIgnoreCase(String name);
}
