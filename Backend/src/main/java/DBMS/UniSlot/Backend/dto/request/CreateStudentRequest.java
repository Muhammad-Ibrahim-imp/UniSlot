package DBMS.UniSlot.Backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO used by admin to register a new student.
 * The service automatically creates a User account
 * with a default password = rollNumber (student must change it).
 */
@Data
public class CreateStudentRequest {

    @NotBlank(message = "Student name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Roll number is required (e.g. CS-2021-001)")
    @Size(max = 50)
    private String rollNumber;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Degree ID is required")
    private Long degreeId;

    @NotNull(message = "Current semester is required")
    @Min(value = 1) @Max(value = 12)
    private Integer currentSemester;
}