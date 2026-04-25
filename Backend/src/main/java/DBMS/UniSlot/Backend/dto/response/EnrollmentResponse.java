package DBMS.UniSlot.Backend.dto.response;


import  DBMS.UniSlot.Backend.enums.LectureDay;
import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;
import java.time.LocalDateTime;

/** One enrolled slot in a student's timetable. */
@Data
@Builder
public class EnrollmentResponse {
    private Long          enrollmentId;
    private String        slotGroupCode;
    private String        courseName;
    private String        courseCode;
    private int           creditHours;
    private String        professorName;
    private LectureDay    dayOfWeek;
    private LocalTime     startTime;
    private LocalTime     endTime;
    private String        venue;
    private LocalDateTime enrolledAt;
    private boolean       dropped;
}
