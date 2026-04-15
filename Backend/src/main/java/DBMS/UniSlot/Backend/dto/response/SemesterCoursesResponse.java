package DBMS.UniSlot.Backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/** Groups courses under their semester number for a degree view. */
@Data
@Builder
public class SemesterCoursesResponse {
    private int                  semesterNumber;
    private List<CourseResponse> courses;
}

