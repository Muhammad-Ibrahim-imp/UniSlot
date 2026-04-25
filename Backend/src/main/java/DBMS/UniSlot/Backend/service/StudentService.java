package DBMS.UniSlot.Backend.service;



import DBMS.UniSlot.Backend.dto.request.CreateStudentRequest;
import DBMS.UniSlot.Backend.dto.request.UpdateFeeStatusRequest;
import DBMS.UniSlot.Backend.dto.response.StudentResponse;

import java.util.List;

/** Contract for student account management and fee operations. */
public interface StudentService {
    StudentResponse create(CreateStudentRequest request);
    StudentResponse getById(Long id);
    StudentResponse getByEmail(String email);  // used after JWT authentication
    List<StudentResponse> getAll();
    List<StudentResponse> getByDepartment(Long departmentId);
    List<StudentResponse> getPaidStudentsOrderedByPaymentTime();
    List<StudentResponse> getUnpaidStudents();
    /** Admin marks a student's fee as paid (or reverts to unpaid). */
    StudentResponse updateFeeStatus(UpdateFeeStatusRequest request);
}

