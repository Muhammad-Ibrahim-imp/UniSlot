package DBMS.UniSlot.Backend.service.Impl;



import DBMS.UniSlot.Backend.dto.request.AddCourseToDegreeRequest;
import DBMS.UniSlot.Backend.dto.request.CreateDegreeRequest;
import DBMS.UniSlot.Backend.dto.response.CourseResponse;
import DBMS.UniSlot.Backend.dto.response.DegreeResponse;
import DBMS.UniSlot.Backend.dto.response.SemesterCoursesResponse;
import DBMS.UniSlot.Backend.entity.*;
import DBMS.UniSlot.Backend.exception.BusinessRuleException;
import DBMS.UniSlot.Backend.exception.ResourceNotFoundException;
import DBMS.UniSlot.Backend.repository.*;
import DBMS.UniSlot.Backend.service.DegreeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DegreeServiceImpl implements DegreeService {

    private final DegreeRepository             degreeRepository;
    private final DepartmentRepository         departmentRepository;
    private final CourseRepository             courseRepository;
    private final DegreeCourseMappingRepository dcmRepository;

    @Override
    @Transactional
    public DegreeResponse create(CreateDegreeRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department", "id", request.getDepartmentId()));

        if (degreeRepository.existsByCodeAndDepartmentId(
                request.getCode(), request.getDepartmentId())) {
            throw new BusinessRuleException(
                    "Degree with code '" + request.getCode() +
                            "' already exists in department '" + department.getName() + "'.");
        }

        Degree degree = Degree.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .durationYears(request.getDurationYears())
                .department(department)
                .build();

        Degree saved = degreeRepository.save(degree);
        log.info("Degree created: {} under {}", saved.getName(), department.getName());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DegreeResponse getById(Long id) {
        Degree degree = degreeRepository.findByIdWithCourseMappings(id)
                .orElseThrow(() -> new ResourceNotFoundException("Degree", "id", id));
        return toResponse(degree);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DegreeResponse> getByDepartment(Long departmentId) {
        return degreeRepository.findByDepartmentId(departmentId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void addCourseToDegree(AddCourseToDegreeRequest request) {
        Degree degree = degreeRepository.findById(request.getDegreeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Degree", "id", request.getDegreeId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course", "id", request.getCourseId()));

        // Validate semester number is within degree bounds
        int maxSemester = degree.getDurationYears() * 2;
        if (request.getSemesterNumber() > maxSemester) {
            throw new BusinessRuleException(
                    "Semester " + request.getSemesterNumber() +
                            " exceeds the maximum semester (" + maxSemester +
                            ") for a " + degree.getDurationYears() + "-year degree.");
        }

        // Prevent duplicate mapping
        if (dcmRepository.existsByDegreeIdAndCourseIdAndSemesterNumber(
                degree.getId(), course.getId(), request.getSemesterNumber())) {
            throw new BusinessRuleException(
                    "Course '" + course.getCourseCode() +
                            "' is already mapped to degree '" + degree.getCode() +
                            "' at semester " + request.getSemesterNumber() + ".");
        }

        DegreeCourseMapping mapping = DegreeCourseMapping.builder()
                .degree(degree)
                .course(course)
                .semesterNumber(request.getSemesterNumber())
                .isCompulsory(request.getIsCompulsory())
                .build();

        dcmRepository.save(mapping);
        log.info("Course {} added to degree {} at semester {}",
                course.getCourseCode(), degree.getCode(), request.getSemesterNumber());
    }

    @Override
    @Transactional
    public void removeCourseFromDegree(Long degreeId, Long courseId) {
        DegreeCourseMapping mapping = dcmRepository
                .findByDegreeIdAndCourseId(degreeId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DegreeCourseMapping", "degreeId+courseId",
                        degreeId + "+" + courseId));
        dcmRepository.delete(mapping);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private DegreeResponse toResponse(Degree degree) {
        // Group course mappings by semester number
        Map<Integer, List<DegreeCourseMapping>> bySemester = degree.getCourseMappings()
                .stream()
                .collect(Collectors.groupingBy(DegreeCourseMapping::getSemesterNumber));

        List<SemesterCoursesResponse> semesters = new ArrayList<>();
        bySemester.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    List<CourseResponse> courses = entry.getValue().stream()
                            .map(m -> CourseResponse.builder()
                                    .id(m.getCourse().getId())
                                    .name(m.getCourse().getName())
                                    .courseCode(m.getCourse().getCourseCode())
                                    .creditHours(m.getCourse().getCreditHours())
                                    .description(m.getCourse().getDescription())
                                    .availableSlotGroups(0) // populated on detailed views
                                    .offeredInDegrees(List.of())
                                    .build())
                            .toList();
                    semesters.add(SemesterCoursesResponse.builder()
                            .semesterNumber(entry.getKey())
                            .courses(courses)
                            .build());
                });

        return DegreeResponse.builder()
                .id(degree.getId())
                .name(degree.getName())
                .code(degree.getCode())
                .durationYears(degree.getDurationYears())
                .departmentName(degree.getDepartment().getName())
                .departmentCode(degree.getDepartment().getCode())
                .semesters(semesters)
                .build();
    }
}