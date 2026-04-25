package DBMS.UniSlot.Backend.service;



import DBMS.UniSlot.Backend.dto.request.CreateProfessorRequest;
import DBMS.UniSlot.Backend.dto.response.ProfessorResponse;

import java.util.List;

/** Contract for professor management and evaluation queries. */
public interface ProfessorService {
    ProfessorResponse create(CreateProfessorRequest request);
    ProfessorResponse getById(Long id);
    List<ProfessorResponse> getAll();
    List<ProfessorResponse> getByDepartment(Long departmentId);
    /** Returns professors ordered by slot fill-rate desc (evaluation). */
    List<ProfessorResponse> getEvaluationRanking();
}
