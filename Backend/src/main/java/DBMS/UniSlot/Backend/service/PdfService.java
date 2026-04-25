package DBMS.UniSlot.Backend.service;


/**
 * Generates a weekly timetable PDF for a student.
 * Returns the path to the generated file so the controller
 * can stream it back as a download.
 */
public interface PdfService {
    /**
     * @param studentEmail — used to load the student + their enrollments.
     * @return absolute file path of the generated PDF.
     */
    String generateTimetablePdf(String studentEmail);
}
