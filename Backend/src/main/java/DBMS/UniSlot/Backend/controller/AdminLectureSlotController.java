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
@Tag(name = "Admin — Lecture Slots", description = "Create and manage lecture slot offerings")
@SecurityRequirement(name = "bearerAuth")
public class AdminLectureSlotController {

    private final LectureSlotService lectureSlotService;

    @PostMapping
    @Operation(summary = "Create a new lecture slot group (one DB row per day specified)")
    public ResponseEntity<ApiResponse<List<LectureSlotResponse>>> create(
            @Valid @RequestBody CreateLectureSlotRequest request) {
        List<LectureSlotResponse> slots = lectureSlotService.createSlotGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Slot group created with " + slots.size() + " day entries", slots));
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
    @Operation(summary = "Delete a slot group (only if no students are enrolled)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String slotGroupCode) {
        lectureSlotService.deleteSlotGroup(slotGroupCode);
        return ResponseEntity.ok(ApiResponse.success("Slot group deleted", null));
    }
}
