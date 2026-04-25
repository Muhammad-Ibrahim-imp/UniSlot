package DBMS.UniSlot.Backend.controller;



import DBMS.UniSlot.Backend.dto.request.SelectSlotRequest;
import DBMS.UniSlot.Backend.dto.response.ApiResponse;
import DBMS.UniSlot.Backend.dto.response.CourseResponse;
import DBMS.UniSlot.Backend.dto.response.EnrollmentResponse;
import DBMS.UniSlot.Backend.dto.response.LectureSlotResponse;
import DBMS.UniSlot.Backend.dto.response.StudentResponse;
import DBMS.UniSlot.Backend.entity.Student;
import DBMS.UniSlot.Backend.entity.User;
import DBMS.UniSlot.Backend.repository.StudentRepository;
import DBMS.UniSlot.Backend.service.CourseService;
import DBMS.UniSlot.Backend.service.LectureSlotService;
import DBMS.UniSlot.Backend.service.PdfGenerationService;
import DBMS.UniSlot.Backend.service.SlotEnrollmentService;
import DBMS.UniSlot.Backend.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * StudentController — Self-service endpoints for logged-in students.
 *
 * All endpoints derive the student identity from the JWT token
 * (@AuthenticationPrincipal User) so students can only access
 * their own data — no studentId path variable is needed or
 * trusted from the client.
 *
 * Base path: /api/students/me
 */
@RestController
@RequestMapping("/api/students/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student — Self Service", description = "Slot selection, timetable and profile for logged-in student")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class StudentController {

    private final StudentService        studentService;
    private final CourseService         courseService;
    private final LectureSlotService    lectureSlotService;
    private final SlotEnrollmentService enrollmentService;
    private final PdfGenerationService  pdfGenerationService;
    private final StudentRepository     studentRepository;

    // ── Profile ───────────────────────────────────────────────────────────

    @GetMapping("/profile")
    @Operation(summary = "Get own student profile")
    public ResponseEntity<ApiResponse<StudentResponse>> getProfile(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved",
                        studentService.getByEmail(user.getEmail())));
    }

    // ── Course Discovery ──────────────────────────────────────────────────

    @GetMapping("/courses")
    @Operation(summary = "Get courses available for my current degree and semester")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getMyCourses(
            @AuthenticationPrincipal User user) {
        Student student = resolveStudent(user);
        List<CourseResponse> courses = courseService.getCoursesForStudentSemester(
                student.getDegree().getId(), student.getCurrentSemester());
        return ResponseEntity.ok(
                ApiResponse.success("Available courses for your semester", courses));
    }

    // ── Slot Selection ────────────────────────────────────────────────────

    @GetMapping("/courses/{courseId}/available-slots")
    @Operation(summary = "Get available (not-full) lecture slots for a specific course")
    public ResponseEntity<ApiResponse<List<LectureSlotResponse>>> getAvailableSlots(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(
                ApiResponse.success("Available slots",
                        lectureSlotService.getAvailableSlotsByCourse(courseId)));
    }

    @PostMapping("/enrollments")
    @Operation(summary = "Select a lecture slot group (fee must be PAID)")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> selectSlot(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SelectSlotRequest request) {
        Student student = resolveStudent(user);
        List<EnrollmentResponse> enrollments =
                enrollmentService.selectSlot(student.getId(), request);
        return ResponseEntity.ok(
                ApiResponse.success("Slot selected successfully. " +
                        enrollments.size() + " day(s) enrolled.", enrollments));
    }

    @DeleteMapping("/enrollments/{slotGroupCode}")
    @Operation(summary = "Drop a previously selected lecture slot")
    public ResponseEntity<ApiResponse<Void>> dropSlot(
            @AuthenticationPrincipal User user,
            @PathVariable String slotGroupCode) {
        Student student = resolveStudent(user);
        enrollmentService.dropSlot(student.getId(), slotGroupCode);
        return ResponseEntity.ok(
                ApiResponse.success("Slot dropped successfully", null));
    }

    @GetMapping("/enrollments")
    @Operation(summary = "View my current timetable (all selected slots)")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyEnrollments(
            @AuthenticationPrincipal User user) {
        Student student = resolveStudent(user);
        return ResponseEntity.ok(
                ApiResponse.success("Your current timetable",
                        enrollmentService.getEnrollmentsForStudent(student.getId())));
    }

    // ── PDF Download ──────────────────────────────────────────────────────

    @GetMapping("/timetable/pdf")
    @Operation(summary = "Download weekly timetable as a PDF file")
    public ResponseEntity<FileSystemResource> downloadTimetablePdf(
            @AuthenticationPrincipal User user) throws IOException {

        Student student = resolveStudent(user);
        String pdfPath  = pdfGenerationService.generateTimetablePdf(student.getId());

        File   pdfFile  = new File(pdfPath);
        String filename = pdfFile.getName();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new FileSystemResource(pdfFile));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Resolves the logged-in User to their Student record.
     * Called on every endpoint to ensure data isolation —
     * a student can only ever operate on their own record.
     */
    private Student resolveStudent(User user) {
        return studentRepository.findByUserEmail(user.getEmail())
                .orElseThrow(() -> new com.university.slotselector.exception
                        .ResourceNotFoundException("Student", "email", user.getEmail()));
    }
}
