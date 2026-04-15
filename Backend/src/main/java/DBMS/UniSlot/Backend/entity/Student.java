package DBMS.UniSlot.Backend.entity;

import DBMS.UniSlot.Backend.enums.FeeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Student Entity  (maps to table: students)
 * ============================================================
 * Represents an enrolled university student.
 *
 * UPDATED to match the full ERD (18-table schema):
 *  - Added FK to DegreeYear (replaces generic currentSemester int)
 *  - Added FK to DegreeYearSection (A/B/C section placement)
 *  - Added cnic and phone fields
 *  - Added relationships to new tables: PaymentInvoice,
 *    TimetablePdf, SlotEnrollment (renamed from StudentSlotEnrollment)
 *
 * Business rules:
 *  1. Only PAID students can select slots (checked via feeStatus).
 *  2. feePaidAt timestamp drives slot selection priority queue.
 *  3. Student is scoped to a specific DegreeYear — this determines
 *     which courses and slots are visible to them.
 * ============================================================
 */
@Entity
@Table(name = "students",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "roll_number"),
                @UniqueConstraint(columnNames = "cnic")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseEntity {

    // ── Identity ──────────────────────────────────────────────

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** University-issued roll number (e.g., "CS-2021-001"). */
    @Column(name = "roll_number", nullable = false, unique = true, length = 50)
    private String rollNumber;

    /** National ID number — used for de-duplication. */
    @Column(name = "cnic", unique = true, length = 13)
    private String cnic;

    @Column(name = "phone", length = 11)
    private String phone;

    // ── Fee ───────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_status", nullable = false, length = 10)
    @Builder.Default
    private FeeStatus feeStatus = FeeStatus.UNPAID;

    /** Set when feeStatus transitions to PAID. Drives priority queue. */
    @Column(name = "fee_paid_at")
    private LocalDateTime feePaidAt;

    // ── Relationships ─────────────────────────────────────────

    /** Login account — created together with the student record. */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Primary department of the student. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /** The degree program (e.g., BSCS). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_program_id", nullable = false)
    private Degree degreeProgram;

    /**
     * The specific year group this student currently belongs to
     * (e.g., "BSCS Year 2"). Determines visible courses and slots.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_year_id", nullable = false)
    private DegreeYear degreeYear;

    /** The class section (A, B, C) within the year group. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private DegreeYearSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_id", nullable = false)
    private Degree degree;

    /** All lecture slot selections made by this student. */
    @OneToMany(mappedBy = "student",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<SlotEnrollment> enrollments = new ArrayList<>();

    /** All payment invoices issued to this student. */
    @OneToMany(mappedBy = "student",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<PaymentInvoice> invoices = new ArrayList<>();

    /** All PDF timetables generated for this student. */
    @OneToMany(mappedBy = "student",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<TimetablePdf> timetablePdfs = new ArrayList<>();

    // ── Helper ───────────────────────────────────────────────

    public boolean hasEligibleFeeStatus() {
        return FeeStatus.PAID.equals(this.feeStatus);
    }
}
