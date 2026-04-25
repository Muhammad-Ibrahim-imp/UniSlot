package DBMS.UniSlot.Backend.service.Impl;



import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import DBMS.UniSlot.Backend.entity.LectureSlot;
import DBMS.UniSlot.Backend.entity.Student;
import DBMS.UniSlot.Backend.enums.LectureDay;
import DBMS.UniSlot.Backend.exception.ResourceNotFoundException;
import DBMS.UniSlot.Backend.repository.LectureSlotRepository;
import DBMS.UniSlot.Backend.repository.StudentRepository;
import DBMS.UniSlot.Backend.service.PdfGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * ============================================================
 * PdfGenerationServiceImpl
 * ============================================================
 * Generates a weekly timetable PDF for a student using iText 7.
 *
 * Layout:
 *   - Header: university branding + student info
 *   - Timetable grid: rows = time slots, columns = days
 *   - Footer: generation timestamp
 *
 * The PDF is written to the configured output directory and
 * the controller returns it as a file download response.
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private final StudentRepository     studentRepository;
    private final LectureSlotRepository lectureSlotRepository;

    @Value("${app.pdf.output-dir}")
    private String pdfOutputDir;

    // Time formatter for display: "08:00 AM"
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("hh:mm a");

    // Brand colour (dark navy blue)
    private static final DeviceRgb HEADER_BG  = new DeviceRgb(23, 37, 84);
    private static final DeviceRgb HEADER_FG  = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb ROW_EVEN   = new DeviceRgb(235, 240, 255);
    private static final DeviceRgb ROW_ODD    = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb SLOT_COLOR = new DeviceRgb(198, 225, 198);

    // Ordered days for the timetable columns
    private static final LectureDay[] ORDERED_DAYS = {
            LectureDay.MONDAY, LectureDay.TUESDAY, LectureDay.WEDNESDAY,
            LectureDay.THURSDAY, LectureDay.FRIDAY, LectureDay.SATURDAY
    };

    @Override
    @Transactional(readOnly = true)
    public String generateTimetablePdf(Long studentId) throws IOException {
        // Load student with their department and degree info
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        // Load all enrolled (not dropped) lecture slots for this student
        List<LectureSlot> slots =
                lectureSlotRepository.findSlotsByStudentEnrollment(studentId);

        if (slots.isEmpty()) {
            throw new IllegalStateException(
                    "Student " + student.getRollNumber() +
                            " has not selected any slots yet. " +
                            "Please complete slot selection before generating the timetable.");
        }

        // Resolve output path: {outputDir}/{rollNumber}_timetable.pdf
        String filename  = student.getRollNumber().replace("/", "-") + "_timetable.pdf";
        Path   outputPath = Paths.get(pdfOutputDir, filename);

        // Build the PDF using iText 7
        try (PdfWriter   writer   = new PdfWriter(outputPath.toString());
             PdfDocument pdfDoc   = new PdfDocument(writer);
             Document    document = new Document(pdfDoc)) {

            // iText default page is A4 portrait; we want landscape for a timetable
            pdfDoc.getDefaultPageSize();
            document.setMargins(30, 30, 30, 30);

            addHeader(document, student);
            addStudentInfo(document, student);
            addTimetableGrid(document, slots);
            addFooter(document);
        }

        log.info("Timetable PDF generated for {}: {}", student.getRollNumber(), outputPath);
        return outputPath.toAbsolutePath().toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private PDF-building helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** University name banner at the top of the document. */
    private void addHeader(Document doc, Student student) {

        // Create a table with 1 column that takes 100% width of the page
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{100}))
                .useAllAvailableWidth();

        // Create a single cell that will act as the header banner
        Cell titleCell = new Cell()

                // Add main title text
                .add(new Paragraph("UNIVERSITY LECTURE TIMETABLE")
                        .setFontSize(20)                    // Large font size
                        .setBold()                          // Bold text
                        .setFontColor(HEADER_FG)            // White text color
                        .setTextAlignment(TextAlignment.CENTER)) // Center align

                // Add subtitle text below the title
                .add(new Paragraph("Slot Selection System — Semester Schedule")
                        .setFontSize(11)                    // Smaller font
                        .setFontColor(HEADER_FG)            // White text
                        .setTextAlignment(TextAlignment.CENTER)) // Center align

                // Set background color (dark navy)
                .setBackgroundColor(HEADER_BG)

                // Add inner spacing inside the cell
                .setPadding(15)

                // Remove border so it looks like a clean banner
                .setBorder(null);

        // Add the header cell into the table
        headerTable.addCell(titleCell);

        // Add the table (header) to the document
        doc.add(headerTable);

        // Add some vertical spacing after header
        doc.add(new Paragraph("\n"));
    }

    /** Student info section below the header */
    private void addStudentInfo(Document doc, Student student) {

        // Create 2-column table (label 25%, value 75%)
        Table info = new Table(UnitValue.createPercentArray(new float[]{25, 75}))
                .useAllAvailableWidth()   // full width
                .setMarginBottom(15);     // space below table

        // Add rows (label + value)
        addInfoRow(info, "Student Name",  student.getName());
        addInfoRow(info, "Roll Number",   student.getRollNumber());
        addInfoRow(info, "Department",    student.getDepartment().getName());
        addInfoRow(info, "Degree",        student.getDegree().getName());
        addInfoRow(info, "Semester",      String.valueOf(student.getCurrentSemester()));
        addInfoRow(info, "Generated On",  LocalDate.now().toString());

        // Add table to document
        doc.add(info);
    }

    private void addInfoRow(Table table, String label, String value) {

        // Add label cell (left column)
        table.addCell(new Cell()
                .add(new Paragraph(label).setBold().setFontSize(10)) // label text
                .setBackgroundColor(ROW_EVEN) // light background
                .setPadding(5) // inner spacing
                .setBorderRight(null)); // remove right border

        // Add value cell (right column)
        table.addCell(new Cell()
                .add(new Paragraph(value != null ? value : "—").setFontSize(10)) // value or fallback
                .setPadding(5) // inner spacing
                .setBorderLeft(null)); // remove left border
    }

    /**
     * Main timetable grid.
     *
     * Approach:
     *  1. Collect all unique (startTime, endTime) pairs across enrolled slots → time rows.
     *  2. Sort them chronologically.
     *  3. Build a 7-column table: Time | Mon | Tue | Wed | Thu | Fri | Sat.
     *  4. For each (day, time) cell, look up whether a slot exists and render it.
     */
    private void addTimetableGrid(Document doc, List<LectureSlot> slots) {
        doc.add(new Paragraph("Weekly Timetable")
                .setFontSize(13).setBold().setMarginBottom(8));

        // Column widths: Time col is narrower, day cols share equal space
        float[] colWidths = {12, 14.7f, 14.7f, 14.7f, 14.7f, 14.7f, 14.7f};
        Table grid = new Table(UnitValue.createPercentArray(colWidths))
                .useAllAvailableWidth();

        // ── Header row ──────────────────────────────────────────────────
        addGridHeaderCell(grid, "Time");
        for (LectureDay day : ORDERED_DAYS) {
            addGridHeaderCell(grid, day.name().substring(0, 3)); // MON, TUE...
        }

        // ── Collect unique time-windows and sort ─────────────────────────
        // Use a record-like pair to group by (start, end)
        record TimeWindow(LocalTime start, LocalTime end) implements Comparable<TimeWindow> {
            public int compareTo(TimeWindow o) {
                return this.start.compareTo(o.start);
            }
        }

        // Flatten all (slot, lecture) pairs into time windows
        SortedSet<TimeWindow> timeWindows = new TreeSet<>();
        for (LectureSlot ls : slots) {
            for (DBMS.UniSlot.Backend.entity.SlotLecture lec : ls.getLectures()) {
                timeWindows.add(new TimeWindow(lec.getStartTime(), lec.getEndTime()));
            }
        }

        // Build lookup: (day, startTime) -> (LectureSlot, SlotLecture)
        record SlotLecturePair(LectureSlot slot, DBMS.UniSlot.Backend.entity.SlotLecture lec) {}
        Map<String, SlotLecturePair> slotMap = new HashMap<>();
        for (LectureSlot ls : slots) {
            for (DBMS.UniSlot.Backend.entity.SlotLecture lec : ls.getLectures()) {
                String key = lec.getDayOfWeek().name() + "|" + lec.getStartTime();
                slotMap.put(key, new SlotLecturePair(ls, lec));
            }
        }

        // ── Data rows ────────────────────────────────────────────────────
        int rowIndex = 0;
        for (TimeWindow tw : timeWindows) {
            String timeLabel = tw.start().format(TIME_FMT) +
                    "\n" + tw.end().format(TIME_FMT);
            DeviceRgb rowBg = (rowIndex % 2 == 0) ? ROW_EVEN : ROW_ODD;

            Cell timeCell = new Cell()
                    .add(new Paragraph(timeLabel).setFontSize(8).setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(rowBg)
                    .setPadding(4)
                    .setTextAlignment(TextAlignment.CENTER);
            grid.addCell(timeCell);

            for (LectureDay day : ORDERED_DAYS) {
                String key = day.name() + "|" + tw.start();
                SlotLecturePair pair = slotMap.get(key);

                if (pair != null) {
                    LectureSlot ls  = pair.slot();
                    DBMS.UniSlot.Backend.entity.SlotLecture lec = pair.lec();
                    String cellText = ls.getSlotName() +
                            "\n" + ls.getCourse().getCourseCode() +
                            "\n" + ls.getProfessor().getName() +
                            (lec.getVenue() != null ? "\n[" + lec.getVenue() + "]" : "");
                    grid.addCell(new Cell()
                            .add(new Paragraph(cellText)
                                    .setFontSize(7.5f)
                                    .setTextAlignment(TextAlignment.CENTER))
                            .setBackgroundColor(SLOT_COLOR)
                            .setPadding(4));
                } else {
                    grid.addCell(new Cell()
                            .add(new Paragraph("—")
                                    .setFontSize(8)
                                    .setTextAlignment(TextAlignment.CENTER)
                                    .setFontColor(ColorConstants.LIGHT_GRAY))
                            .setBackgroundColor(rowBg)
                            .setPadding(4));
                }
            }
            rowIndex++;
        }

        doc.add(grid);

        // ── Credit hour summary ──────────────────────────────────────────
        int totalCredits = slots.stream()
                .mapToInt(ls -> ls.getCourse().getCreditHours())
                .sum();

        doc.add(new Paragraph("\nTotal Credit Hours Selected: " + totalCredits)
                .setFontSize(11).setBold().setMarginTop(10));
    }

    private void addGridHeaderCell(Table table, String text) {
        table.addCell(new Cell()
                .add(new Paragraph(text).setFontSize(9).setBold()
                        .setFontColor(HEADER_FG)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(HEADER_BG)
                .setPadding(6));
    }

    /** Generation timestamp at the bottom of the page. */
    private void addFooter(Document doc) {
        doc.add(new Paragraph(
                "\nThis timetable was generated on " +
                        java.time.LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) +
                        " by the University Slot Selection System.")
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));
    }
}
