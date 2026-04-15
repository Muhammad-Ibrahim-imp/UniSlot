package DBMS.UniSlot.Backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/** Public view of a Course. */
@Data
@Builder
public class CourseResponse {
    private Long   id;
    private String name;
    private String courseCode;
    private int    creditHours;
    private String description;
    /** Number of slot groups available for this course. */
    private int    availableSlotGroups;
    /** Brief list of which degrees offer this course. */
    private List<String> offeredInDegrees;
}
