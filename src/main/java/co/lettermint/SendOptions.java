package co.lettermint;

import java.time.Duration;

/**
 * Options of calls that accept an {@code Idempotency-Key}: {@code emails().send()},
 * {@code emails().sendBatch()}, {@code EmailBuilder.send()} and {@code messages().process()}.
 *
 * <pre>{@code
 * lettermint.emails().send(message, SendOptions.idempotencyKey("order-1234-confirmation"));
 * }</pre>
 *
 * <p>The key applies only to the call it is passed to; the SDK never stores it. Retrying with the same
 * key does not send the email again. The SDK never retries on its own.
 *
 * @param idempotencyKey sent as the {@code Idempotency-Key} header, or null
 * @param timeout overrides the client's timeout for this call, or null
 */
public record SendOptions(String idempotencyKey, Duration timeout) {
    /**
     * @param idempotencyKey sent as the {@code Idempotency-Key} header
     * @return options with this key
     */
    public static SendOptions idempotencyKey(String idempotencyKey) {
        return new SendOptions(idempotencyKey, null);
    }

    /**
     * @param timeout the timeout for this call
     * @return options with this timeout
     */
    public static SendOptions timeout(Duration timeout) {
        return new SendOptions(null, timeout);
    }

    /**
     * @param idempotencyKey sent as the {@code Idempotency-Key} header
     * @return a copy with this key
     */
    public SendOptions withIdempotencyKey(String idempotencyKey) {
        return new SendOptions(idempotencyKey, timeout);
    }

    /**
     * @param timeout the timeout for this call
     * @return a copy with this timeout
     */
    public SendOptions withTimeout(Duration timeout) {
        return new SendOptions(idempotencyKey, timeout);
    }
}
