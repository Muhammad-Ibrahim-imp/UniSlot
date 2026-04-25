package DBMS.UniSlot.Backend.service.Impl;


import DBMS.UniSlot.Backend.dto.request.CreateLectureSlotRequest;
import DBMS.UniSlot.Backend.dto.response.LectureSlotResponse;
import DBMS.UniSlot.Backend.entity.*;
import DBMS.UniSlot.Backend.enums.LectureDay;
import DBMS.UniSlot.Backend.exception.BusinessRuleException;
import DBMS.UniSlot.Backend.exception.ResourceNotFoundException;
import DBMS.UniSlot.Backend.repository.*;
import DBMS.UniSlot.Backend.service.LectureSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LectureSlotServiceImpl implements LectureSlotService {

    private final LectureSlotRepository lectureSlotRepository;
    private final CourseRepository      courseRepository;
    private final ProfessorRepository   professorRepository;

    @Override
    @Transactional
    public List<LectureSlotResponse> createSlotGroup(CreateLectureSlotRequest request) {
        // Validate course and professor exist
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course", "id", request.getCourseId()));
        Professor professor = professorRepository.findById(request.getProfessorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professor", "id", request.getProfessorId()));

        // Validate time range
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessRuleException("End time must be after start time.");
        }

        // Generate a unique group code linking all day-rows of this slot
        String groupCode = course.getCourseCode() + "-" + professor.getId() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        List<LectureSlot> created = new ArrayList<>();

        for (LectureDay day : request.getDays()) {
            // Detect room conflicts on the same day/venue/time
            if (request.getVenue() != null) {
                List<LectureSlot> conflicts = lectureSlotRepository.findConflictingSlots(
                        day, request.getVenue(),
                        request.getStartTime(), request.getEndTime());
                if (!conflicts.isEmpty()) {
                    throw new BusinessRuleException(
                            "Venue '" + request.getVenue() + "' is already booked on " + day +
                                    " from " + request.getStartTime() + " to " + request.getEndTime() + ".");
                }
            }

            LectureSlot slot = LectureSlot.builder()
                    .course(course)
                    .professor(professor)
                    .dayOfWeek(day)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .venue(request.getVenue())
                    .slotGroupCode(groupCode)
                    .maxCapacity(request.getMaxCapacity())
                    .enrolledCount(0)
                    .slotOpenedAt(LocalDateTime.now())
                    .build();

            created.add(lectureSlotRepository.save(slot));
        }

        // Update professor's total seats offered for evaluation metrics
        professor.setTotalSeatsOffered(
                professor.getTotalSeatsOffered() + request.getMaxCapacity());
        professorRepository.save(professor);

        log.info("Slot group {} created for course {} — {} days, {} seats",
                groupCode, course.getCourseCode(), request.getDays().size(),
                request.getMaxCapacity());

        return created.stream().map(this::toResponse).toList();
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
    public void deleteSlotGroup(String slotGroupCode) {
        List<LectureSlot> slots = lectureSlotRepository.findBySlotGroupCode(slotGroupCode);
        if (slots.isEmpty()) {
            throw new ResourceNotFoundException(
                    "LectureSlot group", "slotGroupCode", slotGroupCode);
        }
        // Guard: don't delete a slot that has active enrollments
        boolean hasEnrollments = slots.stream()
                .anyMatch(s -> s.getEnrolledCount() > 0);
        if (hasEnrollments) {
            throw new BusinessRuleException(
                    "Cannot delete slot group '" + slotGroupCode +
                            "': students are already enrolled. Drop their enrollments first.");
        }
        lectureSlotRepository.deleteAll(slots);
        log.info("Slot group {} deleted ({} rows)", slotGroupCode, slots.size());
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private LectureSlotResponse toResponse(LectureSlot ls) {
        return LectureSlotResponse.builder()
                .id(ls.getId())
                .slotGroupCode(ls.getSlotGroupCode())
                .courseName(ls.getCourse().getName())
                .courseCode(ls.getCourse().getCourseCode())
                .professorName(ls.getProfessor().getName())
                .dayOfWeek(ls.getDayOfWeek())
                .startTime(ls.getStartTime())
                .endTime(ls.getEndTime())
                .venue(ls.getVenue())
                .maxCapacity(ls.getMaxCapacity())
                .enrolledCount(ls.getEnrolledCount())
                .availableSeats(ls.getMaxCapacity() - ls.getEnrolledCount())
                .isFull(!ls.hasAvailableSeats())
                .slotOpenedAt(ls.getSlotOpenedAt())
                .slotFilledAt(ls.getSlotFilledAt())
                .build();
    }
}
