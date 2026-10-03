package co.lettermint.exceptions;

/** HTTP 404: the resource does not exist or is not visible to the token. */
public class NotFoundException extends ApiException {
    private static final long serialVersionUID = 1L;

    /**
     * @param status the HTTP status
     * @param message the API's message
     * @param code the error code, or null
     * @param details the error details, or null
     * @param body the decoded body, or null
     */
    public NotFoundException(int status, String message, String code, Object details, Object body) {
        super(status, message, code, details, body);
    }
}
