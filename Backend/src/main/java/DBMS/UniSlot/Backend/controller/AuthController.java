package DBMS.UniSlot.Backend.controller;



import DBMS.UniSlot.Backend.dto.request.LoginRequest;
import DBMS.UniSlot.Backend.dto.response.ApiResponse;
import DBMS.UniSlot.Backend.dto.response.AuthResponse;
import DBMS.UniSlot.Backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — Public endpoint for logging in.
 *
 * POST /api/auth/login
 *   Body: { "email": "...", "password": "..." }
 *   Returns: { "token": "...", "role": "ADMIN|STUDENT", ... }
 *
 * The returned JWT must be sent as:
 *   Authorization: Bearer <token>
 * in all subsequent requests to protected endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and token management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with email and password, returns a JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse auth = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", auth));
    }
}
