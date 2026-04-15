package DBMS.UniSlot.Backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * SemesterFeeStructure Entity  (maps to table: semester_fee_structures)
 * ============================================================
 * Defines the fee breakdown for a specific degree program
 * and year number for a given academic period.
 *
 * Example:
 *   BSCS, Year 2, Semester 2024-I
 *   tuition_fee  = 35,000
 *   exam_fee     =  5,000
 *   lab_fee      =  5,000
 *   total_amount = 45,000
 *
 * When a student is registered for a semester, a
 * PaymentInvoice is generated from this fee structure.
 *
 * valid_from / valid_to allows fee structures to be
 * versioned per academic year without deleting old data.
 * ============================================================
 */
@Entity
@Table(name = "semester_fee_structures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterFeeStructure extends BaseEntity {

    /**
     * The degree program this fee structure applies to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_program_id", nullable = false)
    private Degree degreeProgram;

    /**
     * Year number within the degree (1, 2, 3 or 4).
     * E.g., Year 2 students in BSCS use this fee structure.
     */
    @Column(name = "year_number", nullable = false)
    private Integer yearNumber;

    /**
     * Semester number within the academic year (1 or 2).
     * Combined with yearNumber to uniquely identify the semester.
     */
    @Column(name = "semester_number", nullable = false)
    private Integer semesterNumber;

    /** Sum of all fee components. Stored for quick invoice generation. */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tuition_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal tuitionFee;

    @Column(name = "exam_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal examFee;

    /** Lab fee — may be zero for programs with no lab courses. */
    @Column(name = "lab_fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal labFee = BigDecimal.ZERO;

    /** When this fee structure became effective (academic year start). */
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    /** When this fee structure expires (academic year end). */
    @Column(name = "valid_to")
    private LocalDateTime validTo;

    // ── Relationships ─────────────────────────────────────────

    /**
     * All invoices generated from this fee structure.
     * One invoice per student registered under this fee structure.
     */
    @OneToMany(mappedBy = "feeStructure", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PaymentInvoice> invoices = new ArrayList<>();
}