package co.lettermint.exceptions;

import java.time.Duration;

/** HTTP 5xx: a server error with a JSON or empty body. */
public class ServerException extends ApiException {
    private static final long serialVersionUID = 1L;

    private final Duration retryAfter;

    /**
     * @param status the HTTP status
     * @param message the API's message
     * @param code the error code, or null
     * @param details the error details, or null
     * @param body the decoded body, or null
     */
    public ServerException(int status, String message, String code, Object details, Object body) {
        this(status, message, code, details, body, null);
    }

    /**
     * @param status the HTTP status
     * @param message the API's message
     * @param code the error code, or null
     * @param details the error details, or null
     * @param body the decoded body, or null
     * @param retryAfter the wait from the {@code Retry-After} header, or null
     */
    public ServerException(int status, String message, String code, Object details, Object body, Duration retryAfter) {
        super(status, message, code, details, body);
        this.retryAfter = retryAfter;
    }

    /**
     * @return how long to wait, from the {@code Retry-After} header (seconds or an HTTP date), or null when the API
     *     sent none
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }
}
