package DBMS.UniSlot.Backend.security;



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthFilter — runs once per request BEFORE Spring Security's
 * UsernamePasswordAuthenticationFilter.
 *
 * What it does:
 *  1. Reads the Authorization header.
 *  2. Extracts the Bearer token.
 *  3. Validates the token with JwtUtil.
 *  4. Loads the UserDetails from the DB.
 *  5. Sets the authentication in the SecurityContext so that
 *     downstream filters and controllers know the user is authenticated.
 *
 * If any step fails (missing/invalid/expired token) the filter
 * simply does nothing — the request proceeds unauthenticated
 * and Spring Security's access rules kick in normally (returning 401/403).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Extract token from "Authorization: Bearer <token>" header
        String token = extractToken(request);

        if (token != null) {
            try {
                // Step 2: Get email (username) from token payload
                String email = jwtUtil.extractUsername(token);

                // Step 3: Only authenticate if not already authenticated in this request
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Step 4: Load full user details from DB (roles, enabled flag, etc.)
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    // Step 5: Validate token against user details
                    if (jwtUtil.validateToken(token, userDetails)) {

                        // Step 6: Build authentication object and populate SecurityContext
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("Authenticated user: {}", email);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not authenticate request: {}", e.getMessage());
                // Don't rethrow — let the request proceed unauthenticated
            }
        }

        // Always continue the filter chain (security rules evaluated next)
        filterChain.doFilter(request, response);
    }

    /**
     * Safely extracts the raw JWT from the Authorization header.
     * Returns null if header is missing or malformed.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7); // Strip "Bearer " prefix
        }
        return null;
    }
}
