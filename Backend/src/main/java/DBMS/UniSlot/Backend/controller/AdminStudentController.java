package DBMS.UniSlot.Backend.controller;



import DBMS.UniSlot.Backend.dto.request.CreateStudentRequest;
import DBMS.UniSlot.Backend.dto.request.UpdateFeeStatusRequest;
import DBMS.UniSlot.Backend.dto.response.ApiResponse;
import DBMS.UniSlot.Backend.dto.response.StudentResponse;
import DBMS.UniSlot.Backend.service.StudentService;
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
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Students", description = "Student management and fee status control")
@SecurityRequirement(name = "bearerAuth")
public class AdminStudentController {

    private final StudentService studentService;

    @PostMapping
    @Operation(summary = "Register a new student (creates login account with default password = rollNumber)")
    public ResponseEntity<ApiResponse<StudentResponse>> create(
            @Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student created", studentService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List all students")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Students retrieved", studentService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Student found", studentService.getById(id)));
    }

    @GetMapping("/by-department/{deptId}")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getByDepartment(
            @PathVariable Long deptId) {
        return ResponseEntity.ok(
                ApiResponse.success("Students in department",
                        studentService.getByDepartment(deptId)));
    }

    @GetMapping("/unpaid")
    @Operation(summary = "Finance dashboard — students who have NOT paid their fee")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getUnpaid() {
        return ResponseEntity.ok(
                ApiResponse.success("Unpaid students", studentService.getUnpaidStudents()));
    }

    @GetMapping("/paid-queue")
    @Operation(summary = "Finance dashboard — paid students ordered by payment time (slot selection priority queue)")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getPaidQueue() {
        return ResponseEntity.ok(
                ApiResponse.success("Paid students priority queue",
                        studentService.getPaidStudentsOrderedByPaymentTime()));
    }

    @PatchMapping("/fee-status")
    @Operation(summary = "Mark a student's fee as PAID or UNPAID")
    public ResponseEntity<ApiResponse<StudentResponse>> updateFeeStatus(
            @Valid @RequestBody UpdateFeeStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Fee status updated",
                        studentService.updateFeeStatus(request)));
    }
}

