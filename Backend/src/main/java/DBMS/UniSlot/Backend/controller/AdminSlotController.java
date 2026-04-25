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

/**
 * AdminSlotController — ADMIN ONLY.
 *
 * Manages lecture slot creation and deletion.
 * Students SELECT slots via StudentSlotController.
 */
@RestController
@RequestMapping("/api/admin/slots")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Lecture Slots", description = "Create and manage lecture slots")
@SecurityRequirement(name = "bearerAuth")
public class AdminSlotController {

    private final LectureSlotService slotService;

    @PostMapping
    @Operation(summary = "Create a lecture slot (can span multiple days)",
            description = "Provide a list of days (e.g., [MONDAY, WEDNESDAY]) and a single " +
                    "time range. One slot entry is stored per day, all linked by slotGroupCode.")
    public ResponseEntity<ApiResponse<List<LectureSlotResponse>>> create(
            @Valid @RequestBody CreateLectureSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lecture slot created",
                        slotService.createSlot(request)));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "All slots for a course (admin view — includes full slots)")
    public ResponseEntity<ApiResponse<List<LectureSlotResponse>>> findByCourse(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success("Slots retrieved",
                slotService.findByCourse(courseId)));
    }

    @GetMapping("/professor/{professorId}")
    @Operation(summary = "All slots taught by a professor")
    public ResponseEntity<ApiResponse<List<LectureSlotResponse>>> findByProfessor(
            @PathVariable Long professorId) {
        return ResponseEntity.ok(ApiResponse.success("Slots retrieved",
                slotService.findByProfessor(professorId)));
    }

    @DeleteMapping("/group/{slotGroupCode}")
    @Operation(summary = "Delete an entire slot group (all days). Fails if students enrolled.")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable String slotGroupCode) {
        slotService.deleteSlotGroup(slotGroupCode);
        return ResponseEntity.ok(ApiResponse.success("Slot group deleted: " + slotGroupCode));
    }
}
