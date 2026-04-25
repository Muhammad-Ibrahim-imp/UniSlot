package DBMS.UniSlot.Backend.service.Impl;



import DBMS.UniSlot.Backend.dto.request.CreateStudentRequest;
import DBMS.UniSlot.Backend.dto.request.UpdateFeeStatusRequest;
import DBMS.UniSlot.Backend.dto.response.StudentResponse;
import DBMS.UniSlot.Backend.entity.*;
import DBMS.UniSlot.Backend.enums.FeeStatus;
import DBMS.UniSlot.Backend.enums.Role;
import DBMS.UniSlot.Backend.exception.BusinessRuleException;
import DBMS.UniSlot.Backend.exception.ResourceNotFoundException;
import DBMS.UniSlot.Backend.repository.*;
import DBMS.UniSlot.Backend.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository    studentRepository;
    private final DepartmentRepository departmentRepository;
    private final DegreeRepository     degreeRepository;
    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;

    @Override
    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        // Guard: unique roll number and email
        if (studentRepository.findByRollNumber(request.getRollNumber()).isPresent()) {
            throw new BusinessRuleException(
                    "Student with roll number '" + request.getRollNumber() + "' already exists.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException(
                    "Email '" + request.getEmail() + "' is already in use.");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department", "id", request.getDepartmentId()));

        Degree degree = degreeRepository.findById(request.getDegreeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Degree", "id", request.getDegreeId()));

        // Ensure degree belongs to the chosen department
        if (!degree.getDepartment().getId().equals(department.getId())) {
            throw new BusinessRuleException(
                    "Degree '" + degree.getCode() +
                            "' does not belong to department '" + department.getCode() + "'.");
        }

        /*
         * Create the login account.
         * Default password = rollNumber (student must change on first login).
         * BCrypt hashing is done here so the plain password never touches the DB.
         */
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getRollNumber()))
                .role(Role.STUDENT)
                .active(true)
                .build();

        Student student = Student.builder()
                .name(request.getName())
                .rollNumber(request.getRollNumber())
                .currentSemester(request.getCurrentSemester())
                .feeStatus(FeeStatus.UNPAID)
                .department(department)
                .degree(degree)
                .user(user)
                .build();

        Student saved = studentRepository.save(student);
        log.info("Student created: {} ({}) in {}/{}",
                saved.getName(), saved.getRollNumber(),
                department.getCode(), degree.getCode());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getByEmail(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student", "email", email));
        return toResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getAll() {
        return studentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getByDepartment(Long departmentId) {
        return studentRepository.findByDepartmentId(departmentId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getPaidStudentsOrderedByPaymentTime() {
        /*
         * Used by the finance team dashboard.
         * Also defines the slot-selection priority queue:
         * students who paid earlier get to register first.
         */
        return studentRepository.findPaidStudentsOrderedByPaymentTime()
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getUnpaidStudents() {
        return studentRepository.findAllUnpaidStudentsOrdered()
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public StudentResponse updateFeeStatus(UpdateFeeStatusRequest request) {
        Student student = findById(request.getStudentId());

        student.setFeeStatus(request.getFeeStatus());

        if (FeeStatus.PAID.equals(request.getFeeStatus())) {
            // Record exact payment timestamp for priority queue ordering
            student.setFeePaidAt(LocalDateTime.now());
            log.info("Fee marked as PAID for student: {} at {}",
                    student.getRollNumber(), student.getFeePaidAt());
        } else {
            // Reverting to UNPAID clears the timestamp
            student.setFeePaidAt(null);
            log.info("Fee reverted to UNPAID for student: {}", student.getRollNumber());
        }

        return toResponse(studentRepository.save(student));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }

    private StudentResponse toResponse(Student s) {
        long slotsSelected = s.getEnrollments().stream()
                .filter(e -> !e.isDropped())
                .map(e -> e.getSlotGroupCode())
                .distinct()
                .count();

        return StudentResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .rollNumber(s.getRollNumber())
                .email(s.getUser().getEmail())
                .departmentName(s.getDepartment().getName())
                .departmentCode(s.getDepartment().getCode())
                .degreeName(s.getDegree().getName())
                .degreeCode(s.getDegree().getCode())
                .currentSemester(s.getCurrentSemester())
                .feeStatus(s.getFeeStatus())
                .feePaidAt(s.getFeePaidAt())
                .slotsSelected((int) slotsSelected)
                .build();
    }
}

