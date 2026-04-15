package DBMS.UniSlot.Backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ============================================================
 * TimetablePdf Entity  (maps to table: timetable_pdfs)
 * ============================================================
 * Audit record of every PDF timetable generated for a student.
 *
 * Each generation creates a new row. This allows:
 *  - Students to re-download a previous version.
 *  - Admin to see when schedules were generated.
 *  - The system to serve the latest PDF without regenerating.
 *
 * The actual file lives on disk at file_path.
 * ============================================================
 */
@Entity
@Table(name = "timetable_pdfs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetablePdf extends BaseEntity {

    /**
     * The student whose schedule this PDF represents.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Absolute path on the server filesystem
     * (e.g., /app/pdfs/CS-2022-001_timetable_20240115.pdf).
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * When this PDF was generated.
     * The most recent row per student = the current schedule.
     */
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    /**
     * Human-readable semester label embedded in the PDF header.
     * E.g., "Semester 4 — 2024-I".
     */
    @Column(name = "semester_label", nullable = false, length = 50)
    private String semesterLabel;
}