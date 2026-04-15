package DBMS.UniSlot.Backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO sent by a student to enrol in a lecture slot group.
 *
 * The student submits the slotGroupCode (e.g., "CS301-5-AB12")
 * rather than individual slot IDs so that all days of a weekly
 * slot are enrolled in one atomic operation.
 */
@Data
public class SelectSlotRequest {

    @NotBlank(message = "Slot group code is required")
    private String slotGroupCode;
}