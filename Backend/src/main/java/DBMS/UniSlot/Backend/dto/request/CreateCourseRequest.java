package DBMS.UniSlot.Backend.dto.request;


import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for creating a new Course.
 *
 * A Course is a global entity (e.g., "Database Systems CS301").
 * After creation, it can be linked to multiple degrees via
 * the AddCourseToDegreeRequest.
 */
@Data
public class CreateCourseRequest {

    @NotBlank(message = "Course name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Course code is required (e.g. CS301)")
    @Size(max = 20)
    @Pattern(regexp = "^[A-Z]{2,5}\\d{3,4}$",
            message = "Course code must be 2-5 letters followed by 3-4 digits (e.g. CS301)")
    private String courseCode;

    @NotNull(message = "Credit hours are required")
    @Min(value = 1, message = "Minimum 1 credit hour")
    @Max(value = 4, message = "Maximum 4 credit hours")
    private Integer creditHours;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description; // optional
}
