package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.Student;
import DBMS.UniSlot.Backend.enums.FeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * StudentRepository
 *
 * Key operations:
 *  1. Find student by roll number (admin lookup).
 *  2. Find student by linked user email (post-login profile load).
 *  3. List students by fee status (finance dashboard).
 *  4. List students who have selected all their slots (admin overview).
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByRollNumber(String rollNumber);

    /** Load student profile right after JWT authentication. */
    Optional<Student> findByUserEmail(String email);

    /** All students in a department. */
    List<Student> findByDepartmentId(Long departmentId);

    /** All students enrolled in a specific degree. */
    List<Student> findByDegreeId(Long degreeId);

    /**
     * Students filtered by fee status.
     * Finance team uses UNPAID filter to send reminders.
     * Slot-selection gate uses PAID filter to allow access.
     */
    List<Student> findByFeeStatus(FeeStatus feeStatus);

    /**
     * FINANCE DASHBOARD QUERY:
     * Students who have NOT yet paid, ordered by department.
     */
    @Query("SELECT s FROM Student s " +
            "WHERE s.feeStatus = 'UNPAID' " +
            "ORDER BY s.department.name, s.name")
    List<Student> findAllUnpaidStudentsOrdered();

    /**
     * FINANCE DASHBOARD — Students who paid, ordered by payment time.
     * Earlier payers get priority for slot selection, so this
     * list defines the registration queue.
     */
    @Query("SELECT s FROM Student s " +
            "WHERE s.feeStatus = 'PAID' " +
            "ORDER BY s.feePaidAt ASC")
    List<Student> findPaidStudentsOrderedByPaymentTime();

    /**
     * Check if a student has already selected a slot for a specific
     * course (by slot group code prefix = courseCode).
     * Prevents double-booking the same course.
     */
    @Query("SELECT COUNT(e) > 0 FROM SlotEnrollment e " +
            "WHERE e.student.id = :studentId " +
            "AND e.lectureSlot.course.id = :courseId " +
            "AND e.dropped = false")
    boolean hasStudentEnrolledInCourse(
            @Param("studentId") Long studentId,
            @Param("courseId")  Long courseId);

    /** Count of students by department for admin dashboard. */
    @Query("SELECT s.department.name, COUNT(s) FROM Student s GROUP BY s.department.name")
    List<Object[]> countStudentsByDepartment();
}
