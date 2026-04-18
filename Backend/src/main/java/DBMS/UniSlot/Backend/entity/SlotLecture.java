package DBMS.UniSlot.Backend.entity;

import DBMS.UniSlot.Backend.enums.LectureDay;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * ============================================================
 * SlotLecture  (maps to table: slot_lectures)
 * ============================================================
 * One individual lecture within a LectureSlot.
 *
 * A LectureSlot (e.g., "OOP Slot 2") can have multiple lectures
 * per week, each potentially on a different day, at a different
 * time, and in a different venue:
 *
 *   OOP Slot 2:
 *     → Monday    10:00–12:00  Room 101
 *     → Wednesday 14:00–16:00  Lab 3
 *
 * When a student enrols in "OOP Slot 2", they automatically
 * attend ALL SlotLectures belonging to that slot.
 *
 * There is NO separate capacity per lecture — capacity lives
 * on the parent LectureSlot.
 * ============================================================
 */
@Entity
@Table(name = "slot_lectures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotLecture extends BaseEntity {

    /** The slot this lecture belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_slot_id", nullable = false)
    private LectureSlot lectureSlot;

    /** Day this lecture runs. */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private LectureDay dayOfWeek;

    /** Lecture start time (e.g., 10:00). */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /** Lecture end time (e.g., 12:00). */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Room, lab, or location for this specific lecture.
     * E.g., "Room 101", "CS-Lab-2", "Block-B Auditorium".
     * Can differ from other lectures in the same slot.
     */
    @Column(name = "venue", length = 150)
    private String venue;
}
