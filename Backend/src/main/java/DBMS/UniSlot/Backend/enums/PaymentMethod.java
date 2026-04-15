package DBMS.UniSlot.Backend.enums;

/**
 * Payment method used when recording a transaction.
 * Stored as a STRING in the payment_transactions table.
 */
public enum PaymentMethod {
    CASH,
    BANK_TRANSFER,
    ONLINE
}
