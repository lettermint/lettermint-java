package co.lettermint.exceptions;

/** HTTP 5xx: a server error with a JSON or empty body. */
public class ServerException extends ApiException {
    private static final long serialVersionUID = 1L;

    /**
     * @param status the HTTP status
     * @param message the API's message
     * @param code the error code, or null
     * @param details the error details, or null
     * @param body the decoded body, or null
     */
    public ServerException(int status, String message, String code, Object details, Object body) {
        super(status, message, code, details, body);
    }
}
