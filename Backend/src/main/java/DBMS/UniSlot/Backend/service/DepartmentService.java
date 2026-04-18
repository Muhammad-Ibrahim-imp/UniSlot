package DBMS.UniSlot.Backend.service;


import DBMS.UniSlot.Backend.dto.request.CreateDepartmentRequest;
import DBMS.UniSlot.Backend.dto.response.DepartmentResponse;

import java.util.List;

/** Contract for department CRUD operations. */
public interface DepartmentService {
    DepartmentResponse create(CreateDepartmentRequest request);
    DepartmentResponse getById(Long id);
    List<DepartmentResponse> getAll();
    DepartmentResponse update(Long id, CreateDepartmentRequest request);
    void delete(Long id);
}