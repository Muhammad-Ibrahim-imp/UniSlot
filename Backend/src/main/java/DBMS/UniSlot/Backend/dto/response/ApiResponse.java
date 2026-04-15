package DBMS.UniSlot.Backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API envelope used for ALL responses.
 *
 * Wrapping every response in a consistent structure makes it easy
 * for frontend clients to handle success and error cases uniformly.
 *
 * {
 *   "success": true,
 *   "message": "Department created successfully",
 *   "data": { ... },
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 *
 * @JsonInclude(NON_NULL) — omits null fields (e.g., data is null on errors).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String  message;
    private T       data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /** Convenience factory for success responses. */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true).message(message).data(data).build();
    }

    /** Convenience factory for error responses (no data payload). */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false).message(message).build();
    }
}
