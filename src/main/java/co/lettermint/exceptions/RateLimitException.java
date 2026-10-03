package co.lettermint.exceptions;

import java.time.Duration;

/** HTTP 429: too many requests. */
public class RateLimitException extends ApiException {
    private static final long serialVersionUID = 1L;

    private final Duration retryAfter;

    /**
     * @param status the HTTP status (429)
     * @param message the API's message
     * @param code the error code, or null
     * @param details the error details, or null
     * @param body the decoded body, or null
     * @param retryAfter the wait from the {@code Retry-After} header, or null
     */
    public RateLimitException(int status, String message, String code, Object details, Object body, Duration retryAfter) {
        super(status, message, code, details, body);
        this.retryAfter = retryAfter;
    }

    /**
     * @return how long to wait, from the {@code Retry-After} header (seconds or an HTTP date), or null
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }
}
