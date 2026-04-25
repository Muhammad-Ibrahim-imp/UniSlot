package DBMS.UniSlot.Backend.service;

import DBMS.UniSlot.Backend.dto.request.CreateLectureSlotRequest;
import DBMS.UniSlot.Backend.dto.response.LectureSlotResponse;

import java.util.List;

public interface LectureSlotService {

    /** Create a complete slot with its full schedule. */
    LectureSlotResponse createSlot(CreateLectureSlotRequest request);

    /** All slots for a course (admin). */
    List<LectureSlotResponse> getSlotsByCourse(Long courseId);

    /** Slots with remaining capacity (student selection). */
    List<LectureSlotResponse> getAvailableSlotsByCourse(Long courseId);

    /** Slots taught by a professor. */
    List<LectureSlotResponse> getSlotsByProfessor(Long professorId);

    /** Delete a slot (only if no active enrollments). */
    void deleteSlot(String slotGroupCode);
}
