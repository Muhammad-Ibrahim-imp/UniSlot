package DBMS.UniSlot.Backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * LoginRequest — payload sent to POST /api/auth/login
 *
 * @NotBlank  — field must not be null or whitespace-only.
 * @Email     — validates email format before hitting the service layer.
 * @Size      — password must be at least 6 characters.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
