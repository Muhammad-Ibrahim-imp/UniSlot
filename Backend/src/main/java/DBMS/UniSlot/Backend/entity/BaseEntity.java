package DBMS.UniSlot.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * ============================================================
 * BaseEntity — Shared Audit Fields
 * ============================================================
 * Every entity in this project extends BaseEntity so that
 * created_at and updated_at are automatically maintained
 * by Spring Data JPA auditing (@EnableJpaAuditing).
 *
 *
 * ============================================================
 */
@Getter
@Setter
@MappedSuperclass                          // Not a table; fields are inherited
@EntityListeners(AuditingEntityListener.class) // Wires up @CreatedDate / @LastModifiedDate
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // MySQL uses AUTO_INCREMENT (IDENTITY strategy).

    private Long id;

    /**
     * Set automatically when entity is first persisted.
     * updatable = false → JPA never overwrites this value.
     */

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Updated automatically every time the entity is saved.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
