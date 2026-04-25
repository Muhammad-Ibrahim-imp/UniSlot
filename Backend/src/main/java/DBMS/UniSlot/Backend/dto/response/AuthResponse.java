package DBMS.UniSlot.Backend.dto.response;


import DBMS.UniSlot.Backend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned after a successful login.
 * The client stores the JWT token and sends it in the
 * Authorization: Bearer <token> header for all subsequent requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private Role   role;
    /** Token lifetime in seconds (matches app.jwt.expiration-ms / 1000). */
    private long   expiresInSeconds;
}
