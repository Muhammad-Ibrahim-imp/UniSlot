package DBMS.UniSlot.Backend.entity;


import DBMS.UniSlot.Backend.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ============================================================
 * User Entity — Authentication & Authorization
 * ============================================================
 * Implements Spring Security's UserDetails interface so that
 * Spring Security can load user credentials directly from the DB.
 *
 * Every system user (admin or student) has one User record.
 * The Student entity holds a @OneToOne reference back to User.
 *
 * Roles:
 *   ADMIN   — manages the entire system configuration.
 *   STUDENT — selects lecture slots after fee payment.
 * ============================================================
 */
@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {

    /**
     * Login email address — used as the username.
     */
    @Column(name = "email", nullable = false, unique = true, length = 200)
    private String email;

    /**
     * BCrypt-hashed password. Never store plain text.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * The role determines what API endpoints this user can access.
     * Spring Security reads this via getAuthorities().
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    /**
     * Soft-disable a user without deleting their record.
     * Disabled users cannot log in.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    // ── Spring Security — UserDetails implementation ──────────

    /**
     * Returns a single authority: ROLE_ADMIN or ROLE_STUDENT.
     * Spring Security requires the "ROLE_" prefix for hasRole() checks.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /** Password field for Spring Security. */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /** Username is the email address. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return active; }

    // ── Notification & Audit relationships ───────────────

    /** All notifications sent to this user. */
    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    /** All audit log entries where this user was the actor. */
    @OneToMany(mappedBy = "actor", fetch = FetchType.LAZY)
    @Builder.Default
    private List<AuditLog> auditLogs = new ArrayList<>();
}

