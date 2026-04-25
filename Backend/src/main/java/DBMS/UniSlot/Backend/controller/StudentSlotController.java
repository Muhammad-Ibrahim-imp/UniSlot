package DBMS.UniSlot.Backend.controller;



import DBMS.UniSlot.Backend.dto.request.SelectSlotRequest;
import DBMS.UniSlot.Backend.dto.response.ApiResponse;
import DBMS.UniSlot.Backend.dto.response.CourseSummaryResponse;
import DBMS.UniSlot.Backend.dto.response.EnrollmentResponse;
import DBMS.UniSlot.Backend.dto.response.LectureSlotResponse;
import DBMS.UniSlot.Backend.dto.response.StudentResponse;
import DBMS.UniSlot.Backend.service.CourseService;
import DBMS.UniSlot.Backend.service.LectureSlotService;
import DBMS.UniSlot.Backend.service.SlotEnrollmentService;
import DBMS.UniSlot.Backend.service.StudentService;
import DBMS.UniSlot.Backend.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * StudentSlotController — authenticated students (STUDENT role).
 *
 * This is the primary interface the student interacts with:
 *
 *  1. GET  /api/slots/my-profile       — view own profile + fee status
 *  2. GET  /api/slots/my-courses        — courses for current semester
 *  3. GET  /api/slots/course/{id}/available — available slots for a course
 *  4. POST /api/slots/select            — select a slot
 *  5. POST /api/slots/drop/{groupCode}  — drop a previously selected slot
 *  6. GET  /api/slots/my-schedule       — view full weekly schedule
 *
 * @AuthenticationPrincipal UserDetails — Spring Security injects the
 * currently authenticated user, so we can get their email without
 * needing them to pass it in the request body.
 */
@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
@Tag(name = "Student - Slot Selection", description = "Browse and select lecture slots")
@SecurityRequirement(name = "bearerAuth")
public class StudentSlotController {

    private final SlotEnrollmentService enrollmentService;
    private final CourseService courseService;
    private final LectureSlotService slotService;
    private final StudentService studentService;
    private final StudentRepository studentRepository;

    @GetMapping("/my-profile")
    @Operation(summary = "View your student profile and fee status")
    public ResponseEntity<ApiResponse<StudentResponse>> myProfile(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success("Profile loaded",
                studentService.findByEmail(user.getUsername())));
    }

    @GetMapping("/my-courses")
    @Operation(summary = "List courses available for your degree and current semester",
            description = "Includes shared courses from other departments (same course code). " +
                    "Shows available slot count per course.")
    public ResponseEntity<ApiResponse<List<CourseSummaryResponse>>> myCourses(
            @AuthenticationPrincipal UserDetails user) {
        // Load student to get degree + current semester
        StudentResponse student = studentService.findByEmail(user.getUsername());
        // We need the degreeId — fetch it via the internal findByEmail route
        var courses = courseService.findCoursesForStudent(
                // Note: We embed degreeId in a second call for clarity.
                // In production you'd add degreeId to StudentResponse.
                getDegreeIdFromEmail(user.getUsername()),
                student.getCurrentSemester());
        return ResponseEntity.ok(ApiResponse.success("Your courses", courses));
    }

    @GetMapping("/course/{courseId}/available")
    @Operation(summary = "List available (not full) lecture slots for a course",
            description = "Shows all slot options the student can choose from. " +
                    "Frontend should group results by slotGroupCode.")
    public ResponseEntity<ApiResponse<List<LectureSlotResponse>>> availableSlots(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success("Available slots",
                slotService.findAvailableByCourse(courseId)));
    }

    @PostMapping("/select")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Select a lecture slot for a course",
            description = "Validates: fee paid, slot available, no double-booking, no time conflict. " +
                    "Returns all day-entries of the selected slot group.")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> selectSlot(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody SelectSlotRequest request) {
        List<EnrollmentResponse> enrolled =
                enrollmentService.selectSlot(user.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(
                "Slot selected successfully! " + enrolled.size() + " day(s) added to your schedule.",
                enrolled));
    }

    @PostMapping("/drop/{slotGroupCode}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Drop a previously selected slot")
    public ResponseEntity<ApiResponse<Void>> dropSlot(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String slotGroupCode) {
        enrollmentService.dropSlot(user.getUsername(), slotGroupCode);
        return ResponseEntity.ok(ApiResponse.success("Slot dropped: " + slotGroupCode));
    }

    @GetMapping("/my-schedule")
    @Operation(summary = "View your full weekly schedule (all enrolled slots)")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> mySchedule(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success("Your schedule",
                enrollmentService.getMySchedule(user.getUsername())));
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    /**
     * Gets the student's degree ID from the repository.
     * This is a thin helper; in a real app you'd either put degreeId
     * in the JWT claims or add it to StudentResponse.
     */
    /** Gets the student's degree ID by looking up their profile from the DB. */
    private Long getDegreeIdFromEmail(String email) {
        return studentRepository.findByUserEmail(email)
                .map(s -> s.getDegree().getId())
                .orElseThrow(() -> new com.university.slotselector.exception.ResourceNotFoundException(
                        "Student", "email", email));
    }
}
