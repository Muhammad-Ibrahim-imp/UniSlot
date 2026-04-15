package DBMS.UniSlot.Backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for creating a new Department.
 * Validation annotations run before the service layer is called,
 * returning 400 Bad Request with field-level error messages.
 */
@Data
public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    private String name;

    @NotBlank(message = "Department code is required (e.g. CS, SE)")
    @Size(max = 10, message = "Code cannot exceed 10 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Code must be uppercase letters/numbers only")
    private String code;
}
