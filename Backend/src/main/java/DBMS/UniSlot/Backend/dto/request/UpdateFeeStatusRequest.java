package DBMS.UniSlot.Backend.dto.request;

import DBMS.UniSlot.Backend.enums.FeeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Admin-only DTO to update a student's fee payment status.
 * When status is set to PAID, the feePaidAt timestamp
 * is automatically recorded in the service layer.
 */
@Data
public class UpdateFeeStatusRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Fee status is required (PAID or UNPAID)")
    private FeeStatus feeStatus;
}

