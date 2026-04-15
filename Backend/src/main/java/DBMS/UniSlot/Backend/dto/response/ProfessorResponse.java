package DBMS.UniSlot.Backend.dto.response;

import lombok.Builder;
import lombok.Data;

/** View of a Professor including evaluation analytics. */
@Data
@Builder
public class ProfessorResponse {
    private Long   id;
    private String name;
    private String email;
    private String qualification;
    private String departmentName;
    private int    totalSeatsOffered;
    private int    totalSeatsFilled;
    /** Computed fill rate shown on evaluation dashboard. */
    private double fillRatePercent;
}
