package DBMS.UniSlot.Backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/** DTO used by admin to register a new professor in the system. */
@Data
public class CreateProfessorRequest {

    @NotBlank(message = "Professor name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @Size(max = 300)
    private String qualification; // optional

    /** Optional — a professor can be assigned to a department. */
    private Long departmentId;
}
