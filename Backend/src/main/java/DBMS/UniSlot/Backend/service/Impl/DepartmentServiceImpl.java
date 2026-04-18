package DBMS.UniSlot.Backend.service.Impl;



import DBMS.UniSlot.Backend.dto.request.CreateDepartmentRequest;
import DBMS.UniSlot.Backend.dto.response.DepartmentResponse;
import DBMS.UniSlot.Backend.entity.Department;
import DBMS.UniSlot.Backend.exception.BusinessRuleException;
import DBMS.UniSlot.Backend.exception.ResourceNotFoundException;
import DBMS.UniSlot.Backend.repository.DepartmentRepository;
import DBMS.UniSlot.Backend.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * DepartmentServiceImpl
 *
 * All write operations are @Transactional so that:
 *  - If anything fails mid-save, the DB is rolled back automatically.
 *  - Read operations use readOnly=true for a small performance gain
 *    (Hibernate skips dirty-checking for read-only transactions).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        // Guard: prevent duplicate names and codes
        if (departmentRepository.existsByName(request.getName())) {
            throw new BusinessRuleException(
                    "A department with name '" + request.getName() + "' already exists.");
        }
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new BusinessRuleException(
                    "A department with code '" + request.getCode() + "' already exists.");
        }

        Department dept = Department.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .build();

        Department saved = departmentRepository.save(dept);
        log.info("Department created: {} ({})", saved.getName(), saved.getCode());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, CreateDepartmentRequest request) {
        Department dept = findById(id);

        // Only validate uniqueness if the value actually changed
        if (!dept.getName().equals(request.getName())
                && departmentRepository.existsByName(request.getName())) {
            throw new BusinessRuleException(
                    "Another department with name '" + request.getName() + "' already exists.");
        }

        dept.setName(request.getName());
        dept.setCode(request.getCode().toUpperCase());
        return toResponse(departmentRepository.save(dept));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Department dept = findById(id);
        if (!dept.getDegrees().isEmpty()) {
            throw new BusinessRuleException(
                    "Cannot delete department '" + dept.getName() +
                            "': it still has " + dept.getDegrees().size() + " degree(s). " +
                            "Delete or reassign them first.");
        }
        departmentRepository.delete(dept);
        log.info("Department deleted: {}", dept.getName());
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    /** Maps a Department entity to its response DTO. */
    private DepartmentResponse toResponse(Department d) {
        return DepartmentResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getCode())
                .degreeCount(d.getDegrees().size())
                .studentCount(d.getStudents().size())
                .build();
    }
}