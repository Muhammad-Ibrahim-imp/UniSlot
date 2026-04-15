package DBMS.UniSlot.Backend.dto.response;

import DBMS.UniSlot.Backend.enums.LectureDay;
import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * View of a single LectureSlot row (one day of a weekly slot).
 * The frontend groups these by slotGroupCode to show weekly slots.
 */
@Data
@Builder
public class LectureSlotResponse {
    private Long          id;
    private String        slotGroupCode;
    private String        courseName;
    private String        courseCode;
    private String        professorName;
    private LectureDay    dayOfWeek;
    private LocalTime     startTime;
    private LocalTime     endTime;
    private String        venue;
    private int           maxCapacity;
    private int           enrolledCount;
    private int           availableSeats;
    private boolean       isFull;
    private LocalDateTime slotOpenedAt;
    private LocalDateTime slotFilledAt;
}
