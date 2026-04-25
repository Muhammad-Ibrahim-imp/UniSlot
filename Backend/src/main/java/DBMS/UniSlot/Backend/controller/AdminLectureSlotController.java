package DBMS.UniSlot.Backend.controller;


import DBMS.UniSlot.Backend.dto.request.CreateLectureSlotRequest;
import DBMS.UniSlot.Backend.dto.response.ApiResponse;
import DBMS.UniSlot.Backend.dto.response.LectureSlotResponse;
import DBMS.UniSlot.Backend.service.LectureSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/slots")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Lecture Slots",
        description = "Create and manage course slots (each slot has its own lecture schedule)")
@SecurityRequirement(name = "bearerAuth")
public class AdminLectureSlotController {

    private final LectureSlotService lectureSlotService;

    /**
     * Create one complete slot for a course.
     *
     * A slot = one section students can enrol into (e.g., "OOP Slot 1").
     * The request body includes the full weekly schedule:
     *   schedule: [
     *     { dayOfWeek: "MONDAY",    startTime: "10:00", endTime: "13:00", venue: "Room 101" },
     *     { dayOfWeek: "WEDNESDAY", startTime: "14:00", endTime: "16:00", venue: "Lab 3"   }
     *   ]
     *
     * Multiple calls to this endpoint create multiple independent slots
     * (e.g., Slot 1, Slot 2, …) that students can choose between.
     */
    @PostMapping
    @Operation(summary = "Create a lecture slot with its full weekly schedule")
    public ResponseEntity<ApiResponse<LectureSlotResponse>> create(
            @Valid @RequestBody CreateLectureSlotRequest request) {
        LectureSlotResponse slot = lectureSlotService.createSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Slot '" + slot.getSlotName() + "' created with " +
                                slot.getLectures().size() + " lecture(s)", slot));
    }

    @GetMapping("/by-course/{courseId}")
    @Operation(summary = "Get all slots for a course (admin view with fill stats)")
    public ResponseEntity<ApiResponse<List<LectureSlotResponse>>> getByCourse(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(
                ApiResponse.success("Slots retrieved",
                        lectureSlotService.getSlotsByCourse(courseId)));
    }

    @GetMapping("/by-professor/{professorId}")
    @Operation(summary = "Get all slots taught by a professor")
    public ResponseEntity<ApiResponse<List<LectureSlotResponse>>> getByProfessor(
            @PathVariable Long professorId) {
        return ResponseEntity.ok(
                ApiResponse.success("Professor slots retrieved",
                        lectureSlotService.getSlotsByProfessor(professorId)));
    }

    @DeleteMapping("/group/{slotGroupCode}")
    @Operation(summary = "Delete a slot (only if no students are enrolled)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String slotGroupCode) {
        lectureSlotService.deleteSlot(slotGroupCode);
        return ResponseEntity.ok(ApiResponse.success("Slot deleted", null));
    }
}
