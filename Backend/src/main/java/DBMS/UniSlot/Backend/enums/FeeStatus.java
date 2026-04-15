package DBMS.UniSlot.Backend.enums;

/**
 * Tracks whether a student has cleared their semester fee.
 *
 * UNPAID  — student cannot yet select slots.
 * PARTIAL — student has paid some amount but balance remains;
 *           admin decides whether to grant slot access.
 * PAID    — student is fully eligible to select lecture slots.
 *           Students who pay earlier get priority during
 *           high-demand slot selection windows.
 */
public enum FeeStatus {
    UNPAID,
    PARTIAL,
    PAID
}