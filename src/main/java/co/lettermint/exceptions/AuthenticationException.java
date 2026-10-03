package co.lettermint.exceptions;

/** HTTP 401: the token is missing, invalid or revoked. */
public class AuthenticationException extends ApiException {
    private static final long serialVersionUID = 1L;

    /**
     * @param status the HTTP status
     * @param message the API's message
     * @param code the error code, or null
     * @param details the error details, or null
     * @param body the decoded body, or null
     */
    public AuthenticationException(int status, String message, String code, Object details, Object body) {
        super(status, message, code, details, body);
    }
}
