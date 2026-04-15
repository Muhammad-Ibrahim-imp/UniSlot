package DBMS.UniSlot.Backend.enums;

/**
 * Categories of system-generated notifications.
 * Used by the frontend to render the correct icon and action.
 */
public enum NotificationType {
    /** Semester fee is due or overdue. */
    FEE_DUE,
    /** A slot selection window has opened for the student's degree year. */
    SLOT_OPEN,
    /** A slot group the student was considering is now full. */
    SLOT_FULL,
    /** The student's timetable PDF has been generated and is ready. */
    PDF_READY
}
