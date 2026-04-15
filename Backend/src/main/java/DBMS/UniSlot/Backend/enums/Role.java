package DBMS.UniSlot.Backend.enums;

/**
 * User roles in the system.
 *
 * ADMIN  — university administrator; can create departments,
 *           degrees, courses, professors and manage students.
 * STUDENT — enrolled student; can browse and select slots
 *            after fee payment is verified.
 */
public enum Role {
    ADMIN,
    STUDENT
}
