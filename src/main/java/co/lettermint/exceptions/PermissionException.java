package co.lettermint.exceptions;

/** HTTP 403: the token may not perform this action, or the plan lacks the feature. */
public class PermissionException extends ApiException {
    private static final long serialVersionUID = 1L;

    /**
     * @param status the HTTP status
     * @param message the API's message
     * @param code the error code, or null
     * @param details the error details, or null
     * @param body the decoded body, or null
     */
    public PermissionException(int status, String message, String code, Object details, Object body) {
        super(status, message, code, details, body);
    }
}
