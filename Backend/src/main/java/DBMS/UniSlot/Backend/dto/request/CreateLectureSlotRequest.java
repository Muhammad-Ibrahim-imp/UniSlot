package DBMS.UniSlot.Backend.dto.request;


import DBMS.UniSlot.Backend.enums.LectureDay;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

/**
 * Request to create a complete LectureSlot with its full weekly schedule.
 *
 * Example (OOP Slot 2):
 *   courseId:    3
 *   professorId: 7
 *   slotName:    "Slot 2"
 *   maxCapacity: 50
 *   schedule:
 *     - day: TUESDAY,  start: 09:00, end: 10:00, venue: "Room 201"
 *     - day: WEDNESDAY, start: 14:00, end: 16:00, venue: "Lab 3"
 */
@Data
public class CreateLectureSlotRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Professor ID is required")
    private Long professorId;

    /**
     * Optional human-readable name.
     * Auto-generated as "Slot N" if not provided.
     */
    @Size(max = 100, message = "Slot name cannot exceed 100 characters")
    private String slotName;

    @NotNull(message = "Maximum capacity is required")
    @Min(value = 1,   message = "Capacity must be at least 1")
    @Max(value = 500, message = "Capacity cannot exceed 500")
    private Integer maxCapacity;

    /**
     * One entry per lecture in this slot's weekly schedule.
     * Each entry defines ONE class meeting (day + time + venue).
     * A slot MUST have at least one lecture entry.
     */
    @NotEmpty(message = "At least one lecture schedule entry is required")
    @Valid
    private List<LectureScheduleEntry> schedule;

    // ── Nested DTO ────────────────────────────────────────────────────────

    @Data
    public static class LectureScheduleEntry {

        @NotNull(message = "Day of week is required")
        private LectureDay dayOfWeek;

        @NotNull(message = "Start time is required (HH:mm)")
        private LocalTime startTime;

        @NotNull(message = "End time is required (HH:mm)")
        private LocalTime endTime;

        /** Room or venue for this specific lecture. */
        @Size(max = 150, message = "Venue cannot exceed 150 characters")
        private String venue;
    }
}
