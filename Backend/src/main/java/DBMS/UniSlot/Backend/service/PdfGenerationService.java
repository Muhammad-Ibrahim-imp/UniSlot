package DBMS.UniSlot.Backend.service;



import java.io.IOException;

/**
 * Contract for generating the weekly timetable PDF for a student.
 * The PDF is saved to the configured output directory and the
 * file path is returned so the controller can serve it as a download.
 */
public interface PdfGenerationService {

    /**
     * Generates a weekly timetable PDF for the given student.
     *
     * @param studentId  ID of the student whose schedule to render.
     * @return           Absolute path to the generated PDF file.
     * @throws IOException if the PDF cannot be written to disk.
     */
    String generateTimetablePdf(Long studentId) throws IOException;
}

