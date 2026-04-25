package DBMS.UniSlot.Backend.service;

import DBMS.UniSlot.Backend.dto.request.CreateCourseRequest;
import DBMS.UniSlot.Backend.dto.response.CourseResponse;

import java.util.List;

/** Contract for course management. */
public interface CourseService {
    CourseResponse create(CreateCourseRequest request);
    CourseResponse getById(Long id);
    CourseResponse getByCode(String courseCode);
    List<CourseResponse> getAll();
    /**
     * Returns courses a student is eligible to choose for their
     * current degree and semester.
     */
    List<CourseResponse> getCoursesForStudentSemester(Long degreeId, Integer semester);
    List<CourseResponse> searchByName(String name);
    void delete(Long id);
    // edit: added update method to allow editing course name, code, credits and description
    CourseResponse update(Long id, CreateCourseRequest request);
}
