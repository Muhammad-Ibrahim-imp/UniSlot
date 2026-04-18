package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.SlotEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlotEnrollmentRepository extends JpaRepository<SlotEnrollment, Long> {

    /**
     * All active (not dropped) enrollments for a student.
     * The lectureSlot is fetched eagerly (entity annotation),
     * so lectures are available without extra queries.
     */
    @Query("SELECT e FROM SlotEnrollment e " +
            "JOIN FETCH e.lectureSlot ls " +
            "JOIN FETCH ls.course " +
            "JOIN FETCH ls.professor " +
            "WHERE e.student.id = :studentId " +
            "AND e.dropped = false " +
            "ORDER BY ls.slotName")
    List<SlotEnrollment> findActiveEnrollmentsByStudent(
            @Param("studentId") Long studentId);

    /**
     * Find enrollment by student + slotGroupCode — used for DROP.
     */
    Optional<SlotEnrollment> findByStudentIdAndSlotGroupCodeAndDroppedFalse(
            Long studentId, String slotGroupCode);

    /**
     * Already enrolled in this exact slot?
     */
    boolean existsByStudentIdAndSlotGroupCodeAndDroppedFalse(
            Long studentId, String slotGroupCode);

    /**
     * Has the student already picked ANY slot for this course?
     * Prevents double-enrollment in the same course.
     */
    @Query("SELECT COUNT(e) > 0 FROM SlotEnrollment e " +
            "WHERE e.student.id = :studentId " +
            "AND e.lectureSlot.course.id = :courseId " +
            "AND e.dropped = false")
    boolean existsByStudentAndCourse(
            @Param("studentId") Long studentId,
            @Param("courseId")  Long courseId);

    /** Count active enrollments for a slot (sanity check). */
    long countByLectureSlotIdAndDroppedFalse(Long lectureSlotId);
}