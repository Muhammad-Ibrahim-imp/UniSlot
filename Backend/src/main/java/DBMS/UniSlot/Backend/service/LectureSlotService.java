package DBMS.UniSlot.Backend.service;


import DBMS.UniSlot.Backend.dto.request.CreateLectureSlotRequest;
import DBMS.UniSlot.Backend.dto.response.LectureSlotResponse;

import java.util.List;

/** Contract for lecture slot management. */
public interface LectureSlotService {
    /** Admin creates a new slot group (one row per day). */
    List<LectureSlotResponse> createSlotGroup(CreateLectureSlotRequest request);
    List<LectureSlotResponse> getSlotsByCourse(Long courseId);
    List<LectureSlotResponse> getAvailableSlotsByCourse(Long courseId);
    List<LectureSlotResponse> getSlotsByProfessor(Long professorId);
    void deleteSlotGroup(String slotGroupCode);
}
