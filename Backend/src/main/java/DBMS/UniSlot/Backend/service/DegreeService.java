package DBMS.UniSlot.Backend.service;



import  DBMS.UniSlot.Backend.dto.request.CreateDegreeRequest;
import  DBMS.UniSlot.Backend.dto.request.AddCourseToDegreeRequest;
import  DBMS.UniSlot.Backend.dto.response.DegreeResponse;

import java.util.List;

/** Contract for degree and degree-course-mapping operations. */
public interface DegreeService {
    DegreeResponse create(CreateDegreeRequest request);
    DegreeResponse getById(Long id);
    List<DegreeResponse> getByDepartment(Long departmentId);
    void addCourseToDegree(AddCourseToDegreeRequest request);
    void removeCourseFromDegree(Long degreeId, Long courseId);
}