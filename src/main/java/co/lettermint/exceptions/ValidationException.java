package co.lettermint.exceptions;

import java.util.List;
import java.util.Map;

/** HTTP 422: the API rejected the request data. */
public class ValidationException extends ApiException {
    private static final long serialVersionUID = 1L;

    private final transient Map<String, List<String>> errors;

    /**
     * @param status the HTTP status (422)
     * @param message the API's message
     * @param code the error code, or null
     * @param details the error details, or null
     * @param body the decoded body, or null
     * @param errors field errors from the {@code {"message", "errors"}} body, or null
     */
    public ValidationException(int status, String message, String code, Object details, Object body, Map<String, List<String>> errors) {
        super(status, message, code, details, body);
        this.errors = errors;
    }

    /**
     * @return field errors from the {@code {"message", "errors"}} body, or null when the API sent none
     */
    public Map<String, List<String>> getErrors() {
        return errors;
    }
}
