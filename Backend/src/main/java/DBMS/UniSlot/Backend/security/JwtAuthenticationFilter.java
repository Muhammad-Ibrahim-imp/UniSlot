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
 * ============================================================
 * JwtAuthenticationFilter
 * ============================================================
 * Intercepts EVERY HTTP request exactly once (OncePerRequestFilter).
 * If the request carries a valid JWT in the Authorization header,
 * this filter sets the authenticated user in Spring Security's
 * SecurityContextHolder so that @PreAuthorize checks downstream work.
 *
 * Flow:
 *   1. Extract token from "Authorization: Bearer <token>" header.
 *   2. Validate token via JwtTokenProvider.
 *   3. Load user details from DB.
 *   4. Set authentication in SecurityContext.
 *   5. Pass request to next filter in the chain.
 * ============================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider  jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        // Step 1 — extract the raw token string from the header
        String token = extractTokenFromRequest(request);

        // Step 2 — validate token (checks signature + expiry)
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

            // Step 3 — get email from token and load full UserDetails from DB
            String      email       = jwtTokenProvider.extractEmail(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Step 4 — build Spring Security authentication object
            var auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,                          // credentials null after authentication
                    userDetails.getAuthorities()); // roles: ROLE_ADMIN or ROLE_STUDENT

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Place auth into SecurityContext so @PreAuthorize works
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.debug("Authenticated user: {}", email);
        }

        // Step 5 — continue the filter chain regardless
        filterChain.doFilter(request, response);
    }

    /**
     * Parses the "Authorization" header and returns the token portion.
     * Returns null if the header is absent or does not start with "Bearer ".
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7); // strip "Bearer " prefix
        }
        return null;
    }
}

