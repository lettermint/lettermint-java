package co.lettermint;

import java.time.Duration;

/**
 * Per-call options accepted by every SDK method that makes a request.
 *
 * <pre>{@code
 * lettermint.messages().list(query, RequestOptions.timeout(Duration.ofSeconds(5)));
 * }</pre>
 *
 * <p>To cancel a call, interrupt the calling thread: the SDK aborts the request and throws
 * {@link java.util.concurrent.CancellationException} with the thread's interrupt status set.
 *
 * @param timeout overrides the client's timeout for this call, or null for the client's timeout
 */
public record RequestOptions(Duration timeout) {
    /**
     * @param timeout the timeout for this call; it covers the whole request including the body
     * @return options with this timeout
     */
    public static RequestOptions timeout(Duration timeout) {
        return new RequestOptions(timeout);
    }
}
