package DBMS.UniSlot.Backend.config;



import DBMS.UniSlot.Backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ============================================================
 * SecurityConfig — Spring Security Configuration
 * ============================================================
 * Configures:
 *  - Stateless JWT authentication (no HTTP sessions).
 *  - Public endpoints (login, Swagger).
 *  - Role-based endpoint restrictions (ADMIN vs STUDENT).
 *  - BCrypt password hashing.
 *  - @PreAuthorize / @PostAuthorize method-level security.
 * ============================================================
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize on service/controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService      userDetailsService;

    /**
     * The main security filter chain.
     * Every request goes through this chain of decisions.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for stateless REST APIs (JWT protects us)
                .csrf(AbstractHttpConfigurer::disable)

                // STATELESS — never create or use HTTP sessions
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── Endpoint Authorization Rules ──────────────────────────────
                .authorizeHttpRequests(auth -> auth

                        // Public — no token needed
                        .requestMatchers(
                                "/api/auth/**",       // login
                                "/swagger-ui/**",     // Swagger UI
                                "/swagger-ui.html",
                                "/api-docs/**",       // OpenAPI JSON
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Admin-only management endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Students can read course/slot info (GET only)
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/slots/**").authenticated()

                        // Students enroll in slots
                        .requestMatchers("/api/students/me/**").hasRole("STUDENT")

                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )

                // Register the JWT filter BEFORE Spring's default username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Use our custom DaoAuthenticationProvider
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
     * DaoAuthenticationProvider wires together:
     *  - UserDetailsService (loads user from DB).
     *  - PasswordEncoder   (BCrypt hash comparison).
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * BCryptPasswordEncoder with strength 12.
     * Higher strength = more secure but slower hashing.
     * 12 is a good production balance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Exposes the AuthenticationManager bean used in AuthService
     * to authenticate login requests programmatically.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
