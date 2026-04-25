package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.PaymentInvoice;
import DBMS.UniSlot.Backend.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * PaymentInvoiceRepository
 * Per-student semester billing records.
 */
@Repository
public interface PaymentInvoiceRepository extends JpaRepository<PaymentInvoice, Long> {

    /** All invoices for a student (across all semesters). */
    List<PaymentInvoice> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    /** All unpaid/partial invoices — finance dashboard. */
    List<PaymentInvoice> findByStatusIn(List<InvoiceStatus> statuses);

    /** Find by invoice number (for receipt lookup). */
    Optional<PaymentInvoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    /** Finance dashboard: total count by status. */
    @Query("SELECT i.status, COUNT(i) FROM PaymentInvoice i GROUP BY i.status")
    List<Object[]> countByStatus();

    /**
     * Students whose invoices are UNPAID, ordered by department
     * for the finance follow-up list.
     */
    @Query("SELECT i FROM PaymentInvoice i " +
            "JOIN FETCH i.student s " +
            "JOIN FETCH s.department " +
            "WHERE i.status IN ('UNPAID','PARTIAL') " +
            "ORDER BY s.department.name, s.name")
    List<PaymentInvoice> findAllOutstandingOrdered();
}