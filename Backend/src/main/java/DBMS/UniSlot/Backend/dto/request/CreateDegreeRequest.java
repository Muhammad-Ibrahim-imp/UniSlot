package DBMS.UniSlot.Backend.dto.request;


import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for creating a Degree under a Department.
 *
 * departmentId — which department owns this degree.
 * durationYears — used to validate semester numbers when
 *                 adding courses (max semester = durationYears * 2).
 */
@Data
public class CreateDegreeRequest {

    @NotBlank(message = "Degree name is required (e.g. BS Computer Science)")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Degree code is required (e.g. BSCS)")
    @Size(max = 20)
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Code must be uppercase letters/numbers only")
    private String code;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Duration in years is required")
    @Min(value = 1, message = "Duration must be at least 1 year")
    @Max(value = 6, message = "Duration cannot exceed 6 years")
    private Integer durationYears;
}

