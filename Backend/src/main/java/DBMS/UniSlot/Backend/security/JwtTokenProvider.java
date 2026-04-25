package DBMS.UniSlot.Backend.security;



import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * ============================================================
 * JwtTokenProvider — JWT Creation and Validation
 * ============================================================
 * Handles the complete JWT lifecycle:
 *  1. generateToken()  — creates a signed JWT after login.
 *  2. extractEmail()   — reads the subject (email) from a token.
 *  3. validateToken()  — verifies signature and expiry.
 *
 * The secret key is read from application.yml (app.jwt.secret).
 * JJWT 0.12.x API is used (Keys.hmacShaKeyFor for HMAC-SHA256).
 * ============================================================
 */
@Component
@Slf4j
public class JwtTokenProvider {

    /** Secret key string from config — must be 256-bit (32+ chars). */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Token lifetime in milliseconds (default 24 hours). */
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Derives a SecretKey object from the configured secret string.
     * HMAC-SHA256 requires at minimum a 256-bit (32-byte) key.
     */
    private SecretKey getSigningKey() {
        // Encode the secret as UTF-8 bytes and wrap in a SecretKey
        byte[] keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a JWT token for the authenticated user.
     * Subject = email; expiry = now + jwtExpirationMs.
     *
     * @param authentication Spring Security's Authentication object,
     *                        set after successful username/password check.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())   // email as subject
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())             // HMAC-SHA256 signature
                .compact();
    }

    /**
     * Extract the email (subject) from a JWT token string.
     * Called by JwtAuthenticationFilter on every protected request.
     */
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validate the token's signature and check it has not expired.
     * Returns false (instead of throwing) so the filter can send 401.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is empty or null: {}", e.getMessage());
        }
        return false;
    }

    /** @return token validity in seconds for the AuthResponse body. */
    public long getExpirationSeconds() {
        return jwtExpirationMs / 1000;
    }
}

