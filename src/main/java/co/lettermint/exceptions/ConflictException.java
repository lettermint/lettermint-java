package co.lettermint.exceptions;

/** HTTP 409: the request conflicts with the current state, for example an Idempotency-Key reused with a different body. */
public class ConflictException extends ApiException {
    private static final long serialVersionUID = 1L;

    /**
     * @param status the HTTP status
     * @param message the API's message
     * @param code the error code, or null
     * @param details the error details, or null
     * @param body the decoded body, or null
     */
    public ConflictException(int status, String message, String code, Object details, Object body) {
        super(status, message, code, details, body);
    }
}
