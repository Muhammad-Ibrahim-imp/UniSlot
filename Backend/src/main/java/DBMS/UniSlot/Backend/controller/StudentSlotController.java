package DBMS.UniSlot.Backend.controller;

import DBMS.UniSlot.Backend.dto.request.SelectSlotRequest;
import DBMS.UniSlot.Backend.dto.response.ApiResponse;
import DBMS.UniSlot.Backend.dto.response.CourseResponse;
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
                studentService.getByEmail(user.getUsername())));
    }

    @GetMapping("/my-courses")
    @Operation(summary = "List courses available for your degree and current semester",
            description = "Includes shared courses from other departments (same course code). " +
                    "Shows available slot count per course.")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> myCourses(
            @AuthenticationPrincipal UserDetails user) {
        StudentResponse student = studentService.getByEmail(user.getUsername());
        Long degreeId = getDegreeIdFromEmail(user.getUsername());
        List<CourseResponse> courses = courseService.getCoursesForStudentSemester(
                degreeId, student.getCurrentSemester());
        return ResponseEntity.ok(ApiResponse.success("Your courses", courses));
    }

    @GetMapping("/course/{courseId}/available")
    @Operation(summary = "List available (not full) lecture slots for a course",
            description = "Shows all slot options the student can choose from. " +
                    "Frontend should group results by slotGroupCode.")
    public ResponseEntity<ApiResponse<List<LectureSlotResponse>>> availableSlots(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success("Available slots",
                slotService.getAvailableSlotsByCourse(courseId)));
    }

    @PostMapping("/select")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Select a lecture slot for a course",
            description = "Validates: fee paid, slot available, no double-booking, no time conflict. " +
                    "Returns all day-entries of the selected slot group.")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> selectSlot(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody SelectSlotRequest request) {
        Long studentId = getStudentIdFromEmail(user.getUsername());
        List<EnrollmentResponse> enrolled = enrollmentService.selectSlot(studentId, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Slot enrolled successfully!",
                enrolled));
    }

    @PostMapping("/drop/{slotGroupCode}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Drop a previously selected slot")
    public ResponseEntity<ApiResponse<Void>> dropSlot(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String slotGroupCode) {
        Long studentId = getStudentIdFromEmail(user.getUsername());
        enrollmentService.dropSlot(studentId, slotGroupCode);
        return ResponseEntity.ok(ApiResponse.success("Slot dropped: " + slotGroupCode, null));
    }

    @GetMapping("/my-schedule")
    @Operation(summary = "View your full weekly schedule (all enrolled slots)")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> mySchedule(
            @AuthenticationPrincipal UserDetails user) {
        Long studentId = getStudentIdFromEmail(user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Your schedule",
                enrollmentService.getEnrollmentsForStudent(studentId)));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    /**
     * Gets the student's database ID by looking up their profile from the DB.
     * Used to translate the JWT email into the studentId required by services.
     */
    private Long getStudentIdFromEmail(String email) {
        return studentRepository.findByUserEmail(email)
                .map(s -> s.getId())
                .orElseThrow(() -> new DBMS.UniSlot.Backend.exception.ResourceNotFoundException(
                        "Student", "email", email));
    }

    /** Gets the student's degree ID by looking up their profile from the DB. */
    private Long getDegreeIdFromEmail(String email) {
        return studentRepository.findByUserEmail(email)
                .map(s -> s.getDegree().getId())
                .orElseThrow(() -> new DBMS.UniSlot.Backend.exception.ResourceNotFoundException(
                        "Student", "email", email));
    }
}

