package DBMS.UniSlot.Backend.service.Impl;


import  DBMS.UniSlot.Backend.dto.request.SelectSlotRequest;
import  DBMS.UniSlot.Backend.dto.response.EnrollmentResponse;
import DBMS.UniSlot.Backend.dto.response.SlotLectureResponse;
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
 * Core enrollment logic — enforces all business rules:
 *
 *  Rule 1 — Fee Gate:      Student must have PAID fee status.
 *  Rule 2 — Capacity:      Slot must have available seats.
 *  Rule 3 — No Duplicate:  Cannot pick 2 slots for the same course.
 *  Rule 4 — No Time Clash: New slot lectures must not overlap existing ones.
 *
 * With the redesigned model a student's enrollment creates exactly
 * ONE SlotEnrollment row (not one per day).  The weekly timetable
 * is built by reading enrollment.lectureSlot.lectures.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlotEnrollmentServiceImpl implements SlotEnrollmentService {

    private final StudentRepository        studentRepository;
    private final LectureSlotRepository    lectureSlotRepository;
    private final SlotEnrollmentRepository enrollmentRepository;
    private final ProfessorRepository      professorRepository;

    @Override
    @Transactional
    public List<EnrollmentResponse> selectSlot(Long studentId,
                                               SelectSlotRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student", "id", studentId));

        // Rule 1 — Fee Gate
        if (!student.hasEligibleFeeStatus()) {
            throw new BusinessRuleException(
                    "Your semester fee is unpaid. " +
                            "Please contact the finance office to enable slot selection.");
        }

        LectureSlot slot = lectureSlotRepository
                .findBySlotGroupCode(request.getSlotGroupCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "LectureSlot", "slotGroupCode", request.getSlotGroupCode()));

        // Rule 2 — Capacity
        if (!slot.hasAvailableSeats()) {
            throw new BusinessRuleException(
                    "Slot '" + slot.getSlotName() + "' is full (" +
                            slot.getMaxCapacity() + "/" + slot.getMaxCapacity() + " seats taken).");
        }

        // Rule 3 — No duplicate course
        if (enrollmentRepository.existsByStudentAndCourse(studentId,
                slot.getCourse().getId())) {
            throw new BusinessRuleException(
                    "You have already enrolled in a slot for '" +
                            slot.getCourse().getCourseCode() +
                            "'. Drop your existing slot first if you want to switch.");
        }

        // Rule 4 — Time conflict detection
        // Collect all lecture slots the student is already enrolled in
        List<LectureSlot> currentSlots =
                lectureSlotRepository.findSlotsByStudentEnrollment(studentId);

        for (SlotLecture newLec : slot.getLectures()) {
            for (LectureSlot existingSlot : currentSlots) {
                for (SlotLecture existingLec : existingSlot.getLectures()) {
                    if (existingLec.getDayOfWeek() == newLec.getDayOfWeek()
                            && timesOverlap(newLec, existingLec)) {
                        throw new BusinessRuleException(
                                "Time conflict on " + newLec.getDayOfWeek() +
                                        " (" + newLec.getStartTime() + "–" + newLec.getEndTime() + ")" +
                                        " overlaps with your existing slot for '" +
                                        existingSlot.getCourse().getCourseCode() + "'.");
                    }
                }
            }
        }

        // All rules passed — create ONE enrollment record
        slot.incrementEnrollment();
        lectureSlotRepository.save(slot);

        SlotEnrollment enrollment = SlotEnrollment.builder()
                .student(student)
                .lectureSlot(slot)
                .slotGroupCode(slot.getSlotGroupCode())
                .enrolledAt(LocalDateTime.now())
                .dropped(false)
                .build();
        enrollmentRepository.save(enrollment);

        // Professor analytics
        Professor professor = slot.getProfessor();
        professor.setTotalSeatsFilled(professor.getTotalSeatsFilled() + 1);
        professorRepository.save(professor);

        log.info("Student {} enrolled in slot '{}' ({})",
                student.getRollNumber(), slot.getSlotName(), slot.getSlotGroupCode());

        return List.of(toResponse(enrollment));
    }

    @Override
    @Transactional
    public void dropSlot(Long studentId, String slotGroupCode) {
        SlotEnrollment enrollment = enrollmentRepository
                .findByStudentIdAndSlotGroupCodeAndDroppedFalse(studentId, slotGroupCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment", "slotGroupCode", slotGroupCode));

        enrollment.setDropped(true);
        enrollment.setDroppedAt(LocalDateTime.now());
        enrollmentRepository.save(enrollment);

        LectureSlot slot = enrollment.getLectureSlot();
        slot.decrementEnrollment();
        lectureSlotRepository.save(slot);

        Professor professor = slot.getProfessor();
        professor.setTotalSeatsFilled(
                Math.max(0, professor.getTotalSeatsFilled() - 1));
        professorRepository.save(professor);

        log.info("Student {} dropped slot '{}'", studentId, slotGroupCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsForStudent(Long studentId) {
        return enrollmentRepository.findActiveEnrollmentsByStudent(studentId)
                .stream().map(this::toResponse).toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean timesOverlap(SlotLecture a, SlotLecture b) {
        return a.getStartTime().isBefore(b.getEndTime())
                && a.getEndTime().isAfter(b.getStartTime());
    }

    private EnrollmentResponse toResponse(SlotEnrollment e) {
        LectureSlot slot = e.getLectureSlot();
        List<SlotLectureResponse> lectures = slot.getLectures().stream()
                .map(l -> SlotLectureResponse.builder()
                        .id(l.getId())
                        .dayOfWeek(l.getDayOfWeek())
                        .startTime(l.getStartTime())
                        .endTime(l.getEndTime())
                        .venue(l.getVenue())
                        .build())
                .sorted((a, b) -> a.getDayOfWeek().compareTo(b.getDayOfWeek()))
                .toList();

        return EnrollmentResponse.builder()
                .enrollmentId(e.getId())
                .slotGroupCode(e.getSlotGroupCode())
                .slotName(slot.getSlotName())
                .courseName(slot.getCourse().getName())
                .courseCode(slot.getCourse().getCourseCode())
                .creditHours(slot.getCourse().getCreditHours())
                .professorName(slot.getProfessor().getName())
                .lectures(lectures)
                .enrolledAt(e.getEnrolledAt())
                .dropped(e.isDropped())
                .build();
    }
}

