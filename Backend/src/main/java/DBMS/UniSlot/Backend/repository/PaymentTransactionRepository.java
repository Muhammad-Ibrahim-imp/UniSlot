package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.PaymentTransaction;
import DBMS.UniSlot.Backend.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * PaymentTransactionRepository
 * Individual payment events against an invoice.
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    /** All transactions on a given invoice (for receipt history). */
    List<PaymentTransaction> findByInvoiceIdOrderByCreatedAtDesc(Long invoiceId);

    /** Deduplicate by external reference number. */
    Optional<PaymentTransaction> findByTransactionRef(String transactionRef);

    boolean existsByTransactionRef(String transactionRef);

    /** All successful transactions processed by a finance user. */
    List<PaymentTransaction> findByProcessedByIdAndStatus(Long userId, PaymentStatus status);
}