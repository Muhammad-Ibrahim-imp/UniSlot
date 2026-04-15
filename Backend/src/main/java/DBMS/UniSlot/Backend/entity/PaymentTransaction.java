package DBMS.UniSlot.Backend.entity;

import DBMS.UniSlot.Backend.enums.PaymentMethod;
import DBMS.UniSlot.Backend.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ============================================================
 * PaymentTransaction Entity  (maps to table: payment_transactions)
 * ============================================================
 * Records one individual payment event against a PaymentInvoice.
 *
 * An invoice can have multiple transactions:
 *   Transaction 1 — PKR 20,000 CASH on Jan 5  → invoice PARTIAL
 *   Transaction 2 — PKR 25,000 BANK on Jan 10 → invoice PAID
 *
 * The finance user who recorded the payment is tracked via
 * processedBy (FK to users table) for audit purposes.
 *
 * FAILED transactions are kept for the audit trail — they do
 * NOT update the invoice amountPaid.
 * ============================================================
 */
@Entity
@Table(name = "payment_transactions",
        uniqueConstraints = @UniqueConstraint(columnNames = "transaction_ref"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction extends BaseEntity {

    /**
     * The invoice this payment is being applied to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private PaymentInvoice invoice;

    /**
     * The finance user (or admin) who recorded this payment.
     * References the users table via a FK.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by", nullable = false)
    private User processedBy;

    /**
     * External reference number (bank challan no., receipt no., etc.).
     * Unique to prevent duplicate transaction recording.
     */
    @Column(name = "transaction_ref", nullable = false, unique = true, length = 80)
    private String transactionRef;

    /** Amount paid in this single transaction. */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** How the payment was made. */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    /**
     * SUCCESS — payment verified and applied to invoice.
     * PENDING — submitted but not yet confirmed (e.g. online).
     * FAILED  — rejected/bounced; invoice not updated.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /** When the payment was actually made (may differ from created_at). */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
