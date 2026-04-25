package DBMS.UniSlot.Backend.service;


import DBMS.UniSlot.Backend.dto.request.SelectSlotRequest;
import DBMS.UniSlot.Backend.dto.response.EnrollmentResponse;

import java.util.List;

/**
 * Contract for student slot selection and drop operations.
 * Core business logic: fee check, capacity check, conflict check.
 */
public interface SlotEnrollmentService {

    /**
     * Student selects a lecture slot group.
     * Rules enforced:
     *  1. Student must have PAID fee status.
     *  2. Slot group must have available seats.
     *  3. Student cannot already be enrolled in another slot for the same course.
     *  4. The new slot must not clash with existing slots in the student's timetable.
     */
    List<EnrollmentResponse> selectSlot(Long studentId, SelectSlotRequest request);

    /**
     * Student drops a previously selected slot.
     * Marks the enrollment as dropped and decrements the slot's enrolledCount.
     */
    void dropSlot(Long studentId, String slotGroupCode);

    /** All active (not dropped) enrollments for a student. */
    List<EnrollmentResponse> getEnrollmentsForStudent(Long studentId);
}
