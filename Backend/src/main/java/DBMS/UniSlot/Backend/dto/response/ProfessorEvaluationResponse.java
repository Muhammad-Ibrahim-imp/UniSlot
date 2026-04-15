package DBMS.UniSlot.Backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/** Admin evaluation dashboard — professor leaderboard entry. */
@Data @Builder
public class ProfessorEvaluationResponse {
    private Long professorId;
    private String name;
    private String email;
    private String departmentName;
    private Integer totalSeatsOffered;
    private Integer totalSeatsFilled;
    private Double fillRatePercent;
    private int rank;
    private LocalDateTime fastestFillTime;
}

