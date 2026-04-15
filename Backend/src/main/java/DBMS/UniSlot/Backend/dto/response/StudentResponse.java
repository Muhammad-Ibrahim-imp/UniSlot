package DBMS.UniSlot.Backend.dto.response;

import DBMS.UniSlot.Backend.enums.FeeStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/** View of a Student — used by admin and student profile endpoints. */
@Data
@Builder
public class StudentResponse {
    private Long          id;
    private String        name;
    private String        rollNumber;
    private String        email;
    private String        departmentName;
    private String        departmentCode;
    private String        degreeName;
    private String        degreeCode;
    private int           currentSemester;
    private FeeStatus     feeStatus;
    private LocalDateTime feePaidAt;
    private int           slotsSelected;   // how many slots chosen so far
}
