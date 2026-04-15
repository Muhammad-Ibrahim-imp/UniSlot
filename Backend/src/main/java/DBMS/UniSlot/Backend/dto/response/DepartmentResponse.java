package DBMS.UniSlot.Backend.dto.response;

import lombok.Builder;
import lombok.Data;

/** Flattened view of a Department — safe to send to any role. */
@Data
@Builder
public class DepartmentResponse {
    private Long   id;
    private String name;
    private String code;
    private int    degreeCount;   // how many degrees belong to this dept
    private int    studentCount;  // how many students are enrolled
}