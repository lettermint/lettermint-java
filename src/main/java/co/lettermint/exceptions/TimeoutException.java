package co.lettermint.exceptions;

import java.time.Duration;

/**
 * The request did not complete within the timeout, which covers the whole request: connecting,
 * sending, the response headers and the body. The API may still have processed the request; retry a
 * send with the same idempotency key.
 *
 * <p>Not to be confused with {@link java.util.concurrent.TimeoutException}.
 */
public class TimeoutException extends LettermintException {
    private static final long serialVersionUID = 1L;

    private final Duration timeout;

    /**
     * @param timeout the timeout that elapsed
     */
    public TimeoutException(Duration timeout) {
        super("The request to the Lettermint API timed out after " + timeout.toMillis() + " ms.");
        this.timeout = timeout;
    }

    /**
     * @return the timeout that elapsed
     */
    public Duration getTimeout() {
        return timeout;
    }
}
