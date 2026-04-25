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
}

