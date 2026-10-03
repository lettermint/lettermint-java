package co.lettermint.exceptions;

/**
 * The API answered with an error status (4xx or 5xx) and a JSON or empty body. Subclasses cover the
 * common statuses; any other 4xx is a plain {@code ApiException}.
 */
public class ApiException extends LettermintException {
    private static final long serialVersionUID = 1L;

    private final int status;
    private final String code;
    private final transient Object details;
    private final transient Object body;

    /**
     * @param status the HTTP status
     * @param message the API's message, or {@code HTTP <status>}
     * @param code the machine-readable error code, or null
     * @param details additional context from {@code error.details}, or null
     * @param body the decoded JSON body, or null for an empty body
     */
    public ApiException(int status, String message, String code, Object details, Object body) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
        this.body = body;
    }

    /**
     * @return the HTTP status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return the machine-readable error code from {@code {"error": {"code"}}} or a string {@code error}, or null
     */
    public String getCode() {
        return code;
    }

    /**
     * @return additional context from {@code {"error": {"details"}}} (maps, lists and scalars), or null
     */
    public Object getDetails() {
        return details;
    }

    /**
     * @return the decoded JSON error body (maps, lists and scalars), or null for an empty body
     */
    public Object getBody() {
        return body;
    }
}
