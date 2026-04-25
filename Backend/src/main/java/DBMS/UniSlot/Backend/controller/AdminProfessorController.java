package DBMS.UniSlot.Backend.controller;


import DBMS.UniSlot.Backend.dto.request.CreateProfessorRequest;
import DBMS.UniSlot.Backend.dto.response.ApiResponse;
import DBMS.UniSlot.Backend.dto.response.ProfessorResponse;
import DBMS.UniSlot.Backend.service.ProfessorService;
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
@RequestMapping("/api/admin/professors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Professors", description = "Professor management and evaluation ranking")
@SecurityRequirement(name = "bearerAuth")
public class AdminProfessorController {

    private final ProfessorService professorService;

    @PostMapping
    @Operation(summary = "Register a new professor")
    public ResponseEntity<ApiResponse<ProfessorResponse>> create(
            @Valid @RequestBody CreateProfessorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Professor created",
                        professorService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List all professors")
    public ResponseEntity<ApiResponse<List<ProfessorResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Professors retrieved", professorService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfessorResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Professor found", professorService.getById(id)));
    }

    @GetMapping("/by-department/{deptId}")
    @Operation(summary = "Get professors by department")
    public ResponseEntity<ApiResponse<List<ProfessorResponse>>> getByDepartment(
            @PathVariable Long deptId) {
        return ResponseEntity.ok(
                ApiResponse.success("Professors in department",
                        professorService.getByDepartment(deptId)));
    }

    @GetMapping("/evaluation-ranking")
    @Operation(summary = "Get professors ranked by slot fill-rate (highest first) — for promotion committee")
    public ResponseEntity<ApiResponse<List<ProfessorResponse>>> getEvaluationRanking() {
        return ResponseEntity.ok(
                ApiResponse.success("Evaluation ranking",
                        professorService.getEvaluationRanking()));
    }
}
