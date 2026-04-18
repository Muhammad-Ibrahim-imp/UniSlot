package DBMS.UniSlot.Backend.service.Impl;

import DBMS.UniSlot.Backend.dto.request.LoginRequest;
import DBMS.UniSlot.Backend.dto.response.AuthResponse;
import DBMS.UniSlot.Backend.entity.User;
import DBMS.UniSlot.Backend.repository.UserRepository;
import DBMS.UniSlot.Backend.security.JwtTokenProvider;
import DBMS.UniSlot.Backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * AuthServiceImpl — handles login and JWT generation.
 *
 * Flow:
 *  1. Pass credentials to AuthenticationManager.
 *     It calls UserDetailsService → BCryptPasswordEncoder → pass/fail.
 *  2. On success, generate JWT via JwtTokenProvider.
 *  3. Return AuthResponse with token + metadata.
 *
 * Spring Security throws BadCredentialsException on wrong password,
 * which is caught by GlobalExceptionHandler → 401 response.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider      jwtTokenProvider;
    private final UserRepository        userRepository;

    @Override
    public AuthResponse login(LoginRequest request) {
        // Attempt authentication — throws BadCredentialsException if wrong credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Generate signed JWT token
        String token = jwtTokenProvider.generateToken(authentication);

        // Load user to include role in the response
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(); // guaranteed to exist since authentication passed

        log.info("User logged in: {} with role {}", user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .expiresInSeconds(jwtTokenProvider.getExpirationSeconds())
                .build();
    }
}