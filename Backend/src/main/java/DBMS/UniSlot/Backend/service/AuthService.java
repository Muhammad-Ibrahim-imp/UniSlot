package DBMS.UniSlot.Backend.service;



import DBMS.UniSlot.Backend.dto.request.LoginRequest;
import DBMS.UniSlot.Backend.dto.response.AuthResponse;

/**
 * AuthService — contract for authentication operations.
 * Only login is needed since admin creates accounts (no self-registration).
 */
public interface AuthService {
    AuthResponse login(LoginRequest request);
}
