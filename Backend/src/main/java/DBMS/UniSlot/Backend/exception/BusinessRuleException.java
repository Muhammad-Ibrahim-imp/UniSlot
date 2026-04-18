package DBMS.UniSlot.Backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a business rule is violated.
 * Examples:
 *  - Student tries to select a slot without paying fee.
 *  - Admin tries to add a duplicate course code.
 *  - Student tries to select a slot that is already full.
 *  - Schedule time conflict detected.
 *
 * Maps to HTTP 409 Conflict (the request conflicts with current state).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}