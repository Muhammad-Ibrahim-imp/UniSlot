package DBMS.UniSlot.Backend.security;



import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JwtUtil — creates and validates JWT tokens.
 *
 * Flow:
 *  1. User logs in → generateToken() returns a signed JWT.
 *  2. On every subsequent request, the filter calls validateToken()
 *     and extractUsername() to authenticate without hitting the DB.
 *
 * Algorithm: HS256 (HMAC-SHA256) using the secret from application.yml.
 */
@Slf4j
@Component
public class JwtUtil {

    /** 256-bit secret key from application.yml — KEEP THIS SECRET IN PRODUCTION. */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Token validity in milliseconds (default 24 hours). */
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // ── Token Creation ────────────────────────────────────────────────────────

    /**
     * Generates a signed JWT for the given user.
     * Subject = email (our unique username).
     * Expires in jwtExpirationMs milliseconds from now.
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())        // email as subject
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())                 // HMAC-SHA256 signing
                .compact();
    }

    // ── Token Validation ──────────────────────────────────────────────────────

    /**
     * Validates the token: checks signature + expiry.
     * @return true if the token is valid for the given user.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /** Extract the email (subject) from the token. */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Derives the SecretKey from the base64-encoded secret string.
     * Keys.hmacShaKeyFor() ensures at least 256-bit key for HS256.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

