package DBMS.UniSlot.Backend.service.Impl;


import DBMS.UniSlot.Backend.entity.LectureSlot;
import DBMS.UniSlot.Backend.entity.Student;
import DBMS.UniSlot.Backend.enums.LectureDay;
import DBMS.UniSlot.Backend.exception.ResourceNotFoundException;
import DBMS.UniSlot.Backend.repository.LectureSlotRepository;
import DBMS.UniSlot.Backend.repository.StudentRepository;
import DBMS.UniSlot.Backend.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PdfServiceImpl — generates a weekly timetable PDF for a student.
 *
 * OUTPUT FORMAT:
 *   - Cover section: student name, roll number, degree, semester.
 *   - Weekly timetable grid: rows = time slots, columns = days.
 *   - Each enrolled slot is filled in the grid with:
 *       course name + professor name + venue.
 *   - Footer: total credit hours enrolled.
 *
 * IMPLEMENTATION APPROACH:
 *   We build the PDF as raw HTML content (a styled table) and
 *   write it using iText's HtmlConverter. This is simpler than
 *   iText's low-level API for table layouts and produces
 *   professional-looking output with minimal code.
 *
 *   The generated file is saved to the configured output directory
 *   (app.pdf.output-dir) and its path is returned to the controller
 *   which streams it back as a download attachment.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final StudentRepository studentRepo;
    private final LectureSlotRepository slotRepo;

    @Value("${app.pdf.output-dir}")
    private String pdfOutputDir;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    /** Days to display as timetable columns (in order). */
    private static final List<LectureDay> ORDERED_DAYS = List.of(
            LectureDay.MONDAY, LectureDay.TUESDAY, LectureDay.WEDNESDAY,
            LectureDay.THURSDAY, LectureDay.FRIDAY, LectureDay.SATURDAY);

    @Override
    @Transactional(readOnly = true)
    public String generateTimetablePdf(String studentEmail) {

        // ── Load data ────────────────────────────────────────────────────────
        Student student = studentRepo.findByUserEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "email", studentEmail));

        List<LectureSlot> enrolledSlots =
                slotRepo.findSlotsByStudentEnrollment(student.getId());

        if (enrolledSlots.isEmpty()) {
            throw new RuntimeException("No slots enrolled yet. Please select your courses first.");
        }

        // Group slots by slotGroupCode to get unique weekly entries
        // Then index by (day, timeKey) for grid rendering
        Map<String, List<LectureSlot>> byGroup = enrolledSlots.stream()
                .collect(Collectors.groupingBy(LectureSlot::getSlotGroupCode));

        // Build a Set of unique time ranges (for row headers)
        TreeSet<String> timeKeys = enrolledSlots.stream()
                .map(ls -> ls.getStartTime().format(TIME_FMT) + " - " + ls.getEndTime().format(TIME_FMT))
                .collect(Collectors.toCollection(TreeSet::new));

        // Map (day -> timeKey -> slot info string)
        Map<LectureDay, Map<String, String>> grid = new HashMap<>();
        for (LectureDay day : ORDERED_DAYS) {
            grid.put(day, new HashMap<>());
        }
        for (LectureSlot ls : enrolledSlots) {
            String timeKey = ls.getStartTime().format(TIME_FMT) + " - " + ls.getEndTime().format(TIME_FMT);
            String cellContent = "<b>" + ls.getCourse().getName() + "</b><br/>"
                    + ls.getProfessor().getName() + "<br/>"
                    + (ls.getVenue() != null ? ls.getVenue() : "");
            grid.get(ls.getDayOfWeek()).put(timeKey, cellContent);
        }

        // Calculate total credit hours
        long totalCredits = byGroup.values().stream()
                .map(slots -> slots.get(0).getCourse().getCreditHours())
                .reduce(0, Integer::sum);

        // ── Build HTML ────────────────────────────────────────────────────────
        String html = buildHtml(student, timeKeys, grid, totalCredits);

        // ── Write PDF ─────────────────────────────────────────────────────────
        String fileName = "timetable_" + student.getRollNumber().replace("/", "-") + ".pdf";
        Path outputPath = Paths.get(pdfOutputDir, fileName).toAbsolutePath().normalize();

        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            com.itextpdf.html2pdf.HtmlConverter.convertToPdf(html, fos);
        } catch (IOException e) {
            log.error("Failed to generate PDF for student {}: {}", studentEmail, e.getMessage());
            throw new RuntimeException("PDF generation failed: " + e.getMessage());
        }

        log.info("PDF generated for student {}: {}", student.getRollNumber(), outputPath);
        return outputPath.toString();
    }

    // ── HTML Builder ───────────────────────────────────────────────────────────

    private String buildHtml(Student student,
                             TreeSet<String> timeKeys,
                             Map<LectureDay, Map<String, String>> grid,
                             long totalCredits) {

        StringBuilder sb = new StringBuilder();

        // HTML head with embedded CSS
        sb.append("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"/>
            <style>
              body { font-family: Arial, sans-serif; font-size: 11px; margin: 20px; }
              h1   { color: #003366; font-size: 18px; }
              h3   { color: #555; margin: 2px 0; }
              .info-table { width: 100%; margin-bottom: 20px; }
              .info-table td { padding: 3px 8px; }
              .info-table .label { font-weight: bold; color: #003366; width: 160px; }
              .timetable { width: 100%; border-collapse: collapse; margin-top: 10px; }
              .timetable th { background: #003366; color: white; padding: 8px 4px;
                              text-align: center; font-size: 10px; border: 1px solid #aaa; }
              .timetable td { border: 1px solid #aaa; padding: 6px 4px;
                              vertical-align: top; font-size: 9px; }
              .time-col  { background: #f0f4ff; font-weight: bold; text-align: center;
                           width: 100px; white-space: nowrap; }
              .slot-cell { background: #e8f5e9; text-align: center; }
              .empty     { background: #fafafa; }
              .footer    { margin-top: 16px; font-size: 11px; color: #333; }
            </style>
            </head><body>
            """);

        // Header
        sb.append("<h1>&#127979; University Lecture Timetable</h1>");
        sb.append("<table class='info-table'>");
        appendRow(sb, "Student Name",   student.getName());
        appendRow(sb, "Roll Number",    student.getRollNumber());
        appendRow(sb, "Department",     student.getDepartment().getName());
        appendRow(sb, "Degree",         student.getDegree().getName());
        appendRow(sb, "Semester",       student.getCurrentSemester().toString());
        appendRow(sb, "Total Credits",  totalCredits + " credit hours");
        sb.append("</table>");

        sb.append("<h3>Weekly Schedule</h3>");

        // Timetable grid
        sb.append("<table class='timetable'>");
        // Header row
        sb.append("<tr><th>Time</th>");
        for (LectureDay day : ORDERED_DAYS) {
            sb.append("<th>").append(day.name(), 0, 3).append("</th>");
        }
        sb.append("</tr>");

        // Data rows — one per time slot
        for (String timeKey : timeKeys) {
            sb.append("<tr>");
            sb.append("<td class='time-col'>").append(timeKey).append("</td>");
            for (LectureDay day : ORDERED_DAYS) {
                String cell = grid.get(day).get(timeKey);
                if (cell != null) {
                    sb.append("<td class='slot-cell'>").append(cell).append("</td>");
                } else {
                    sb.append("<td class='empty'>&nbsp;</td>");
                }
            }
            sb.append("</tr>");
        }
        sb.append("</table>");

        // Footer
        sb.append("<div class='footer'>Generated on: ")
                .append(java.time.LocalDate.now())
                .append(" &nbsp;|&nbsp; This is a system-generated document.</div>");
        sb.append("</body></html>");

        return sb.toString();
    }

    private void appendRow(StringBuilder sb, String label, String value) {
        sb.append("<tr><td class='label'>").append(label)
                .append(":</td><td>").append(value).append("</td></tr>");
    }
}

