package DBMS.UniSlot.Backend.dto.response;

import DBMS.UniSlot.Backend.enums.LectureDay;
import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;

/** One lecture entry within a slot's weekly schedule. */
@Data
@Builder
public class SlotLectureResponse {
    private Long       id;
    private LectureDay dayOfWeek;
    private LocalTime  startTime;
    private LocalTime  endTime;
    private String     venue;
}
