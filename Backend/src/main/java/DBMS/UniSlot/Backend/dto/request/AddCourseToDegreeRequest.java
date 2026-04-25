package DBMS.UniSlot.Backend.dto.request;



import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Links an existing Course to an existing Degree at a specific semester.
 * This creates a DegreeCourseMapping record.
 *
 * Example use: Admin assigns PHY101 to BSCS at semester 4,
 * and also to BSSE at semester 2 — two separate calls, same courseId.
 */
@Data
public class AddCourseToDegreeRequest {

    @NotNull(message = "Degree ID is required")
    private Long degreeId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Semester number is required")
    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 12, message = "Semester cannot exceed 12")
    private Integer semesterNumber;

    /** Defaults to true if not provided. */
    private Boolean isCompulsory = true;
}
