package DBMS.UniSlot.Backend.service.Impl;


import DBMS.UniSlot.Backend.dto.request.CreateProfessorRequest;
import DBMS.UniSlot.Backend.dto.response.ProfessorResponse;
import DBMS.UniSlot.Backend.entity.Department;
import DBMS.UniSlot.Backend.entity.Professor;
import DBMS.UniSlot.Backend.exception.BusinessRuleException;
import DBMS.UniSlot.Backend.exception.ResourceNotFoundException;
import DBMS.UniSlot.Backend.repository.DepartmentRepository;
import DBMS.UniSlot.Backend.repository.ProfessorRepository;
import DBMS.UniSlot.Backend.service.ProfessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessorServiceImpl implements ProfessorService {

    private final ProfessorRepository  professorRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public ProfessorResponse create(CreateProfessorRequest request) {
        if (professorRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException(
                    "A professor with email '" + request.getEmail() + "' already exists.");
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.getDepartmentId()));
        }

        Professor professor = Professor.builder()
                .name(request.getName())
                .email(request.getEmail())
                .qualification(request.getQualification())
                .department(department)
                .build();

        Professor saved = professorRepository.save(professor);
        log.info("Professor created: {}", saved.getName());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfessorResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessorResponse> getAll() {
        return professorRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessorResponse> getByDepartment(Long departmentId) {
        return professorRepository.findByDepartmentId(departmentId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessorResponse> getEvaluationRanking() {
        /*
         * Returns professors sorted by fill-rate descending.
         * Professors whose slots fill fastest appear at the top —
         * indicating higher student preference → better evaluation score.
         */
        return professorRepository.findAllOrderedByFillRateDesc()
                .stream().map(this::toResponse).toList();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Professor findById(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", id));
    }

    private ProfessorResponse toResponse(Professor p) {
        return ProfessorResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .email(p.getEmail())
                .qualification(p.getQualification())
                .departmentName(p.getDepartment() != null ? p.getDepartment().getName() : null)
                .totalSeatsOffered(p.getTotalSeatsOffered())
                .totalSeatsFilled(p.getTotalSeatsFilled())
                .fillRatePercent(p.getFillRatePercent())
                .build();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", id));
        boolean hasActiveSlots = !professor.getLectureSlots().isEmpty() &&
                professor.getLectureSlots().stream().anyMatch(s -> s.getEnrolledCount() > 0);
        if (hasActiveSlots) {
            throw new BusinessRuleException(
                    "Cannot delete professor '" + professor.getName() + "': they have slots with " +
                            "enrolled students. Drop those enrollments first.");
        }
        professorRepository.delete(professor);
        log.info("Professor {} deleted", professor.getName());
    }

    // edit: new update method — allows admin to edit professor name, email, qualification and department
    @Override
    @Transactional
    public ProfessorResponse update(Long id, CreateProfessorRequest request) {
        // edit: find existing professor or 404
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", id));
        // edit: if email changed, ensure it's not taken by another professor
        if (!professor.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (professorRepository.existsByEmail(request.getEmail())) {
                throw new BusinessRuleException(
                        "Email '" + request.getEmail() + "' is already registered to another professor.");
            }
        }
        // edit: apply updated values
        professor.setName(request.getName().trim());
        professor.setEmail(request.getEmail().trim().toLowerCase());
        professor.setQualification(request.getQualification());
        // edit: update department (can be set to null to unassign)
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.getDepartmentId()));
            professor.setDepartment(dept);
        } else {
            professor.setDepartment(null); // edit: allow un-assigning from department
        }
        professor = professorRepository.save(professor); // edit: persist
        log.info("Professor {} updated", professor.getName()); // edit: audit
        return toResponse(professor); // edit: return updated DTO
    }
}
