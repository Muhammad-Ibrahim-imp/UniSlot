////////c
package DBMS.UniSlot.Backend.controller;



import DBMS.UniSlot.Backend.dto.request.CreateCourseRequest;
import DBMS.UniSlot.Backend.dto.response.ApiResponse;
import DBMS.UniSlot.Backend.dto.response.CourseResponse;
import DBMS.UniSlot.Backend.service.CourseService;
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
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Courses", description = "Course creation and management")
@SecurityRequirement(name = "bearerAuth")
public class AdminCourseController {

    private final CourseService courseService;

    @PostMapping
    @Operation(summary = "Create a new course (globally unique by course code)")
    public ResponseEntity<ApiResponse<CourseResponse>> create(
            @Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created", courseService.create(request)));
    }

    @GetMapping
    @Operation(summary = "Get all courses in the system")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Courses retrieved", courseService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Course found", courseService.getById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search courses by name (partial, case-insensitive)")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> search(
            @RequestParam String name) {
        return ResponseEntity.ok(
                ApiResponse.success("Search results", courseService.searchByName(name)));
    }
}
