package DBMS.UniSlot.Backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/** View of a Degree with its course catalog. */
@Data
@Builder
public class DegreeResponse {
    private Long   id;
    private String name;
    private String code;
    private int    durationYears;
    private String departmentName;
    private String departmentCode;
    /** Courses grouped by semester number. */
    private List<SemesterCoursesResponse> semesters;
}