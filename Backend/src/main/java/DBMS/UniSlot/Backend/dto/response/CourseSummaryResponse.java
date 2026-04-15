package DBMS.UniSlot.Backend.dto.response;

import lombok.Builder;
import lombok.Data;

/** Lightweight course info used inside Degree or Slot listings. */
@Data @Builder
public class CourseSummaryResponse {
    private Long id;
    private String name;
    private String courseCode;
    private Integer creditHours;
    private Integer semesterNumber;
    private Boolean isCompulsory;
    private int availableSlots;
}
