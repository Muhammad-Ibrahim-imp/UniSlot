package DBMS.UniSlot.Backend.service.Impl;

import DBMS.UniSlot.Backend.dto.request.CreateLectureSlotRequest;
import DBMS.UniSlot.Backend.dto.response.LectureSlotResponse;
import DBMS.UniSlot.Backend.dto.response.SlotLectureResponse;
import DBMS.UniSlot.Backend.entity.*;
import DBMS.UniSlot.Backend.exception.BusinessRuleException;
import DBMS.UniSlot.Backend.exception.ResourceNotFoundException;
import DBMS.UniSlot.Backend.repository.*;
import DBMS.UniSlot.Backend.service.LectureSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureSlotServiceImpl implements LectureSlotService {

    private final LectureSlotRepository  lectureSlotRepository;
    private final SlotLectureRepository  slotLectureRepository;
    private final CourseRepository       courseRepository;
    private final ProfessorRepository    professorRepository;

    @Override
    @Transactional
    public LectureSlotResponse createSlot(CreateLectureSlotRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course", "id", request.getCourseId()));
        Professor professor = professorRepository.findById(request.getProfessorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professor", "id", request.getProfessorId()));

        // Validate each lecture entry
        for (var entry : request.getSchedule()) {
            if (!entry.getEndTime().isAfter(entry.getStartTime())) {
                throw new BusinessRuleException(
                        "End time must be after start time for " + entry.getDayOfWeek() + ".");
            }
            // Venue conflict check (only if venue is specified)
            if (entry.getVenue() != null && !entry.getVenue().isBlank()) {
                List<SlotLecture> conflicts = slotLectureRepository.findConflictingVenueBookings(
                        entry.getDayOfWeek(), entry.getVenue(),
                        entry.getStartTime(), entry.getEndTime());
                if (!conflicts.isEmpty()) {
                    throw new BusinessRuleException(
                            "Venue '" + entry.getVenue() + "' is already booked on " +
                                    entry.getDayOfWeek() + " from " + entry.getStartTime() +
                                    " to " + entry.getEndTime() + ".");
                }
            }
            // edit: professor time-conflict check — a professor cannot teach two overlapping slots
            List<LectureSlot> professorSlots = lectureSlotRepository.findByProfessorId(
                    request.getProfessorId()); // edit: get all existing slots for this professor
            for (LectureSlot existingSlot : professorSlots) { // edit: iterate each existing slot
                for (SlotLecture existingLec : existingSlot.getLectures()) { // edit: each lecture in slot
                    if (existingLec.getDayOfWeek() == entry.getDayOfWeek() // edit: same day
                            && existingLec.getStartTime().isBefore(entry.getEndTime()) // edit: overlap start
                            && existingLec.getEndTime().isAfter(entry.getStartTime())) { // edit: overlap end
                        throw new BusinessRuleException( // edit: reject with clear message
                                "Professor already has a slot on " + entry.getDayOfWeek() +
                                        " from " + existingLec.getStartTime() + " to " + existingLec.getEndTime() +
                                        " ('" + existingSlot.getSlotName() + "' for " +
                                        existingSlot.getCourse().getCourseCode() + ")." +
                                        " A professor cannot teach two overlapping slots.");
                    }
                }
            }
        }

        // Auto-generate slot name if not provided
        String slotName = (request.getSlotName() != null && !request.getSlotName().isBlank())
                ? request.getSlotName().trim()
                : generateSlotName(course.getCourseCode(), professor.getId());

        // Generate unique group code
        String groupCode = course.getCourseCode() + "-"
                + professor.getId() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Create the slot (container)
        LectureSlot slot = LectureSlot.builder()
                .course(course)
                .professor(professor)
                .slotName(slotName)
                .slotGroupCode(groupCode)
                .maxCapacity(request.getMaxCapacity())
                .enrolledCount(0)
                .slotOpenedAt(LocalDateTime.now())
                .build();
        slot = lectureSlotRepository.save(slot);

        // Create one SlotLecture per schedule entry
        final LectureSlot savedSlot = slot;
        for (var entry : request.getSchedule()) {
            SlotLecture lecture = SlotLecture.builder()
                    .lectureSlot(savedSlot)
                    .dayOfWeek(entry.getDayOfWeek())
                    .startTime(entry.getStartTime())
                    .endTime(entry.getEndTime())
                    .venue(entry.getVenue())
                    .build();
            slotLectureRepository.save(lecture);
        }

        // Refresh to include the saved lectures
        slot = lectureSlotRepository.findById(slot.getId()).orElseThrow();

        // Update professor analytics: count this slot's capacity
        professor.setTotalSeatsOffered(
                professor.getTotalSeatsOffered() + request.getMaxCapacity());
        professorRepository.save(professor);

        log.info("Slot '{}' ({}) created for {} — {} lectures, capacity {}",
                slotName, groupCode, course.getCourseCode(),
                request.getSchedule().size(), request.getMaxCapacity());

        return toResponse(slot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureSlotResponse> getSlotsByCourse(Long courseId) {
        return lectureSlotRepository.findByCourseId(courseId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureSlotResponse> getAvailableSlotsByCourse(Long courseId) {
        return lectureSlotRepository.findAvailableSlotsByCourse(courseId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureSlotResponse> getSlotsByProfessor(Long professorId) {
        return lectureSlotRepository.findByProfessorId(professorId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteSlot(String slotGroupCode) {
        LectureSlot slot = lectureSlotRepository.findBySlotGroupCode(slotGroupCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "LectureSlot", "slotGroupCode", slotGroupCode));
        if (slot.getEnrolledCount() > 0) {
            throw new BusinessRuleException(
                    "Cannot delete slot '" + slotGroupCode +
                            "': " + slot.getEnrolledCount() + " student(s) enrolled. " +
                            "Drop their enrollments first.");
        }
        lectureSlotRepository.delete(slot);
        log.info("Slot {} deleted", slotGroupCode);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateSlotName(String courseCode, Long professorId) {
        long existing = lectureSlotRepository.findByCourseId(
                        courseRepository.findByCourseCode(courseCode)
                                .map(c -> c.getId()).orElse(0L))
                .size();
        return "Slot " + (existing + 1);
    }

    LectureSlotResponse toResponse(LectureSlot slot) {
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

        return LectureSlotResponse.builder()
                .id(slot.getId())
                .slotGroupCode(slot.getSlotGroupCode())
                .slotName(slot.getSlotName())
                .courseName(slot.getCourse().getName())
                .courseCode(slot.getCourse().getCourseCode())
                .professorName(slot.getProfessor().getName())
                .professorEmail(slot.getProfessor().getEmail())
                .maxCapacity(slot.getMaxCapacity())
                .enrolledCount(slot.getEnrolledCount())
                .availableSeats(slot.getMaxCapacity() - slot.getEnrolledCount())
                .isFull(!slot.hasAvailableSeats())
                .slotOpenedAt(slot.getSlotOpenedAt())
                .slotFilledAt(slot.getSlotFilledAt())
                .lectures(lectures)
                .build();
    }
}
