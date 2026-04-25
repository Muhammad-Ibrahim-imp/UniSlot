package DBMS.UniSlot.Backend.controller;

import DBMS.UniSlot.Backend.dto.request.AddCourseToDegreeRequest;
import DBMS.UniSlot.Backend.dto.request.CreateDegreeRequest;
import DBMS.UniSlot.Backend.dto.response.ApiResponse;
import DBMS.UniSlot.Backend.dto.response.DegreeResponse;
import DBMS.UniSlot.Backend.service.DegreeService;
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
@RequestMapping("/api/admin/degrees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Degrees", description = "Degree programs and course-to-degree mapping")
@SecurityRequirement(name = "bearerAuth")
public class AdminDegreeController {

    private final DegreeService degreeService;

    @PostMapping
    @Operation(summary = "Create a new degree under a department")
    public ResponseEntity<ApiResponse<DegreeResponse>> create(
            @Valid @RequestBody CreateDegreeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Degree created", degreeService.create(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get degree details with its full course catalog by semester")
    public ResponseEntity<ApiResponse<DegreeResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Degree found", degreeService.getById(id)));
    }

    @GetMapping("/by-department/{departmentId}")
    @Operation(summary = "Get all degrees under a department")
    public ResponseEntity<ApiResponse<List<DegreeResponse>>> getByDepartment(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(
                ApiResponse.success("Degrees retrieved",
                        degreeService.getByDepartment(departmentId)));
    }

    @PostMapping("/add-course")
    @Operation(summary = "Assign a course to a degree at a specific semester number")
    public ResponseEntity<ApiResponse<Void>> addCourse(
            @Valid @RequestBody AddCourseToDegreeRequest request) {
        degreeService.addCourseToDegree(request);
        return ResponseEntity.ok(
                ApiResponse.success("Course added to degree at semester " +
                        request.getSemesterNumber(), null));
    }


    // edit: added PUT endpoint to update degree name, code and duration years
    @PutMapping("/{id}")
    @Operation(summary = "Update a degree's name, code or duration")
    public ResponseEntity<ApiResponse<DegreeResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateDegreeRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Degree updated", degreeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a degree (only if no students are enrolled in it)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        degreeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Degree deleted", null));
    }

    @DeleteMapping("/{degreeId}/courses/{courseId}")
    @Operation(summary = "Remove a course from a degree")
    public ResponseEntity<ApiResponse<Void>> removeCourse(
            @PathVariable Long degreeId,
            @PathVariable Long courseId) {
        degreeService.removeCourseFromDegree(degreeId, courseId);
        return ResponseEntity.ok(ApiResponse.success("Course removed from degree", null));
    }
}
