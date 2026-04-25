package DBMS.UniSlot.Backend.service.Impl;



import DBMS.UniSlot.Backend.dto.request.CreateCourseRequest;
import DBMS.UniSlot.Backend.dto.response.CourseResponse;
import DBMS.UniSlot.Backend.entity.Course;
import DBMS.UniSlot.Backend.entity.DegreeCourseMapping;
import DBMS.UniSlot.Backend.exception.BusinessRuleException;
import DBMS.UniSlot.Backend.exception.ResourceNotFoundException;
import DBMS.UniSlot.Backend.repository.CourseRepository;
import DBMS.UniSlot.Backend.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public CourseResponse create(CreateCourseRequest request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new BusinessRuleException(
                    "Course with code '" + request.getCourseCode() + "' already exists. " +
                            "Use AddCourseToDegree to link it to another degree.");
        }
        Course course = Course.builder()
                .name(request.getName())
                .courseCode(request.getCourseCode().toUpperCase())
                .creditHours(request.getCreditHours())
                .description(request.getDescription())
                .build();
        Course saved = courseRepository.save(course);
        log.info("Course created: {} ({})", saved.getName(), saved.getCourseCode());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getByCode(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course", "courseCode", courseCode));
        return toResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAll() {
        return courseRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesForStudentSemester(Long degreeId, Integer semester) {
        /*
         * Returns all courses assigned to this degree at this semester.
         * Includes courses shared from other departments
         * (identified by same courseCode in different degree mappings).
         */
        return courseRepository.findByDegreeAndSemester(degreeId, semester)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> searchByName(String name) {
        return courseRepository.findByNameContainingIgnoreCase(name)
                .stream().map(this::toResponse).toList();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
    }

    private CourseResponse toResponse(Course c) {
        // Collect names of degrees that offer this course
        List<String> degrees = c.getDegreeMappings().stream()
                .map(m -> m.getDegree().getCode() + " (Sem " + m.getSemesterNumber() + ")")
                .toList();

        // Count distinct slot groups by unique group codes
        long slotGroupCount = c.getLectureSlots().stream()
                .map(ls -> ls.getSlotGroupCode())
                .distinct()
                .count();

        return CourseResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .courseCode(c.getCourseCode())
                .creditHours(c.getCreditHours())
                .description(c.getDescription())
                .availableSlotGroups((int) slotGroupCount)
                .offeredInDegrees(degrees)
                .build();
    }
}

