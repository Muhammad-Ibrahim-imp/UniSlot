package DBMS.UniSlot.Backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * View of one enrollment in a student's timetable.
 * Contains the full slot detail (name, professor, all lectures).
 */
@Data
@Builder
public class EnrollmentResponse {
    private Long                      enrollmentId;
    private String                    slotGroupCode;
    private String                    slotName;
    private String                    courseName;
    private String                    courseCode;
    private int                       creditHours;
    private String                    professorName;

    /** Full weekly schedule: all lectures in this slot. */
    private List<SlotLectureResponse> lectures;

    private LocalDateTime             enrolledAt;
    private boolean                   dropped;
}