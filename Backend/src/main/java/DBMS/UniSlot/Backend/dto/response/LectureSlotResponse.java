package DBMS.UniSlot.Backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Full view of a LectureSlot shown to admin (all slots) and
 * students (available slots).
 *
 * The lectures list contains one entry per scheduled class
 * meeting (day + time + venue).  The student picks ONE slot
 * and attends every lecture in its list.
 */
@Data
@Builder
public class LectureSlotResponse {
    private Long                      id;
    private String                    slotGroupCode;
    private String                    slotName;
    private String                    courseName;
    private String                    courseCode;
    private String                    professorName;
    private String                    professorEmail;
    private int                       maxCapacity;
    private int                       enrolledCount;
    private int                       availableSeats;
    private boolean                   isFull;
    private LocalDateTime             slotOpenedAt;
    private LocalDateTime             slotFilledAt;

    /** All lectures belonging to this slot. */
    private List<SlotLectureResponse> lectures;
}