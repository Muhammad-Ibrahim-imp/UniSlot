package DBMS.UniSlot.Backend.enums;

/**
 * Tracks how much of a payment invoice has been settled.
 * UNPAID  — no payment received yet.
 * PARTIAL — some payment received but balance > 0.
 * PAID    — fully settled; student can select slots.
 */
public enum InvoiceStatus {
    UNPAID,
    PARTIAL,
    PAID
}