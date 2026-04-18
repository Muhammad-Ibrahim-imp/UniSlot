package DBMS.UniSlot.Backend.repository;

import DBMS.UniSlot.Backend.entity.SlotLecture;
import DBMS.UniSlot.Backend.enums.LectureDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface SlotLectureRepository extends JpaRepository<SlotLecture, Long> {

    List<SlotLecture> findByLectureSlotId(Long lectureSlotId);

    /**
     * Venue conflict detection: is this room already booked on
     * the given day + overlapping time window?
     */
    @Query("SELECT sl FROM SlotLecture sl " +
            "WHERE sl.dayOfWeek = :day " +
            "AND sl.venue = :venue " +
            "AND sl.startTime < :endTime " +
            "AND sl.endTime > :startTime")
    List<SlotLecture> findConflictingVenueBookings(
            @Param("day")       LectureDay day,
            @Param("venue")     String venue,
            @Param("startTime") LocalTime startTime,
            @Param("endTime")   LocalTime endTime);
}
