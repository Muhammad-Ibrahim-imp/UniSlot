package DBMS.UniSlot.Backend.service.Impl;


import  DBMS.UniSlot.Backend.dto.request.SelectSlotRequest;
import  DBMS.UniSlot.Backend.dto.response.EnrollmentResponse;
import  DBMS.UniSlot.Backend.entity.*;
import  DBMS.UniSlot.Backend.exception.BusinessRuleException;
import  DBMS.UniSlot.Backend.exception.ResourceNotFoundException;
import  DBMS.UniSlot.Backend.repository.*;
import  DBMS.UniSlot.Backend.service.SlotEnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * SlotEnrollmentServiceImpl — Core Business Logic
 * ============================================================
 * This is the HEART of the application. The selectSlot() method
 * enforces all business rules:
 *
 *  Rule 1 — Fee Gate:     Student must have PAID fee status.
 *  Rule 2 — Capacity:     Slot must have available seats.
 *  Rule 3 — No Duplicate: Student cannot pick 2 slots for same course.
 *  Rule 4 — No Time Clash: New slot must not overlap existing timetable.
 *
 * @Transactional on selectSlot() is critical:
 *   If two students try to grab the last seat simultaneously,
 *   the DB-level transaction isolation (READ_COMMITTED by default
 *   on PostgreSQL) + the enrolledCount increment + flush ensures
 *   only one succeeds. For truly high-traffic systems, consider
 *   a pessimistic lock (@Lock(PESSIMISTIC_WRITE)) on the LectureSlot.
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlotEnrollmentServiceImpl implements SlotEnrollmentService {

    private final StudentRepository             studentRepository;
    private final LectureSlotRepository         lectureSlotRepository;
    private final StudentSlotEnrollmentRepository enrollmentRepository;
    private final ProfessorRepository           professorRepository;

    @Override
    @Transactional
    public List<EnrollmentResponse> selectSlot(Long studentId, SelectSlotRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        // ── Rule 1: Fee Gate ──────────────────────────────────────────────
        if (!student.hasEligibleFeeStatus()) {
            throw new BusinessRuleException(
                    "You cannot select slots until your semester fee is paid. " +
                            "Please contact the finance office.");
        }

        // Fetch all slots in the requested group (one per day)
        List<LectureSlot> slotGroup = lectureSlotRepository
                .findBySlotGroupCode(request.getSlotGroupCode());

        if (slotGroup.isEmpty()) {
            throw new ResourceNotFoundException(
                    "LectureSlot group", "slotGroupCode", request.getSlotGroupCode());
        }

        // ── Rule 3: No Duplicate Course ──────────────────────────────────
        // All slots in a group belong to the same course, so check using the first slot
        Long courseId = slotGroup.get(0).getCourse().getId();
        if (enrollmentRepository.existsByStudentAndCourse(studentId, courseId)) {
            throw new BusinessRuleException(
                    "You have already selected a slot for course '" +
                            slotGroup.get(0).getCourse().getCourseCode() +
                            "'. Drop the existing slot first if you want to switch.");
        }

        // ── Rule 2: Capacity Check (use first slot — all share same capacity) ─
        LectureSlot representative = slotGroup.get(0);
        if (!representative.hasAvailableSeats()) {
            throw new BusinessRuleException(
                    "Slot '" + request.getSlotGroupCode() + "' is full (" +
                            representative.getMaxCapacity() + "/" +
                            representative.getMaxCapacity() + " seats taken).");
        }

        // ── Rule 4: Time Conflict Check ───────────────────────────────────
        // Get the student's current timetable slots
        List<LectureSlot> currentSlots =
                lectureSlotRepository.findSlotsByStudentEnrollment(studentId);

        for (LectureSlot newSlot : slotGroup) {
            for (LectureSlot existing : currentSlots) {
                if (existing.getDayOfWeek() == newSlot.getDayOfWeek()
                        && timesOverlap(newSlot, existing)) {
                    throw new BusinessRuleException(
                            "Time conflict: the selected slot on " + newSlot.getDayOfWeek() +
                                    " (" + newSlot.getStartTime() + "–" + newSlot.getEndTime() + ")" +
                                    " overlaps with your existing slot for '" +
                                    existing.getCourse().getCourseCode() + "'.");
                }
            }
        }

        // ── All rules passed — enroll the student ────────────────────────
        // Update each slot's enrollment count
        for (LectureSlot slot : slotGroup) {
            slot.incrementEnrollment();
            lectureSlotRepository.save(slot);
        }

        // Update professor's total seats filled metric (once per group, not per day-row)
        Professor professor = slotGroup.get(0).getProfessor();
        professor.setTotalSeatsFilled(professor.getTotalSeatsFilled() + 1);
        professorRepository.save(professor);

        // Create one enrollment record per slot-day
        List<StudentSlotEnrollment> enrollments = slotGroup.stream()
                .map(slot -> StudentSlotEnrollment.builder()
                        .student(student)
                        .lectureSlot(slot)
                        .slotGroupCode(request.getSlotGroupCode())
                        .enrolledAt(LocalDateTime.now())
                        .dropped(false)
                        .build())
                .map(enrollmentRepository::save)
                .toList();

        log.info("Student {} enrolled in slot group {} ({} days)",
                student.getRollNumber(), request.getSlotGroupCode(), slotGroup.size());

        return enrollments.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void dropSlot(Long studentId, String slotGroupCode) {
        // Find all enrollment rows for this student + group
        List<StudentSlotEnrollment> enrollments =
                enrollmentRepository.findActiveEnrollmentsByStudent(studentId)
                        .stream()
                        .filter(e -> e.getSlotGroupCode().equals(slotGroupCode))
                        .toList();

        if (enrollments.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Active enrollment", "slotGroupCode", slotGroupCode);
        }

        for (StudentSlotEnrollment enrollment : enrollments) {
            // Soft-delete the enrollment
            enrollment.setDropped(true);
            enrollment.setDroppedAt(LocalDateTime.now());
            enrollmentRepository.save(enrollment);

            // Decrement the slot's enrollment count
            LectureSlot slot = enrollment.getLectureSlot();
            slot.decrementEnrollment();
            lectureSlotRepository.save(slot);
        }

        // Decrement professor fill count
        Professor professor = enrollments.get(0).getLectureSlot().getProfessor();
        professor.setTotalSeatsFilled(
                Math.max(0, professor.getTotalSeatsFilled() - 1));
        professorRepository.save(professor);

        log.info("Student {} dropped slot group {}", studentId, slotGroupCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsForStudent(Long studentId) {
        return enrollmentRepository.findActiveEnrollmentsByStudent(studentId)
                .stream().map(this::toResponse).toList();
    }

    // ── Private helpers ────────────────────────────────────────────────────

    /**
     * Checks whether two lecture slots have overlapping time ranges on the same day.
     * Overlap exists when: a.start < b.end AND a.end > b.start
     */
    private boolean timesOverlap(LectureSlot a, LectureSlot b) {
        return a.getStartTime().isBefore(b.getEndTime())
                && a.getEndTime().isAfter(b.getStartTime());
    }

    private EnrollmentResponse toResponse(StudentSlotEnrollment e) {
        LectureSlot ls = e.getLectureSlot();
        return EnrollmentResponse.builder()
                .enrollmentId(e.getId())
                .slotGroupCode(e.getSlotGroupCode())
                .courseName(ls.getCourse().getName())
                .courseCode(ls.getCourse().getCourseCode())
                .creditHours(ls.getCourse().getCreditHours())
                .professorName(ls.getProfessor().getName())
                .dayOfWeek(ls.getDayOfWeek())
                .startTime(ls.getStartTime())
                .endTime(ls.getEndTime())
                .venue(ls.getVenue())
                .enrolledAt(e.getEnrolledAt())
                .dropped(e.isDropped())
                .build();
    }
}
