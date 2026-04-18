package DBMS.UniSlot.Backend.repository;


import  DBMS.UniSlot.Backend.entity.LectureSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureSlotRepository extends JpaRepository<LectureSlot, Long> {

    /** All slots for a course — admin view. */
    List<LectureSlot> findByCourseId(Long courseId);

    /** All slots taught by a professor. */
    List<LectureSlot> findByProfessorId(Long professorId);

    /** Find a slot by its unique group code. */
    Optional<LectureSlot> findBySlotGroupCode(String slotGroupCode);

    /**
     * Available slots for a course (capacity not exhausted).
     * Shown to students during slot selection.
     */
    @Query("SELECT ls FROM LectureSlot ls " +
            "WHERE ls.course.id = :courseId " +
            "AND ls.enrolledCount < ls.maxCapacity " +
            "ORDER BY ls.slotName")
    List<LectureSlot> findAvailableSlotsByCourse(@Param("courseId") Long courseId);

    /**
     * Load all slots a student is actively enrolled in.
     * Used for timetable building and conflict detection.
     */
    @Query("SELECT ls FROM LectureSlot ls " +
            "JOIN ls.enrollments e " +
            "WHERE e.student.id = :studentId " +
            "AND e.dropped = false")
    List<LectureSlot> findSlotsByStudentEnrollment(@Param("studentId") Long studentId);
}
