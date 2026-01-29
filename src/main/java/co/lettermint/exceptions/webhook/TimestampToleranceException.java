package co.lettermint.exceptions.webhook;

/**
 * Exception thrown when webhook timestamp is outside the allowed tolerance window.
 */
public class TimestampToleranceException extends WebhookVerificationException {

    private final long timestamp;
    private final long tolerance;

    public TimestampToleranceException(String message, long timestamp, long tolerance) {
        super(message);
        this.timestamp = timestamp;
        this.tolerance = tolerance;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTolerance() {
        return tolerance;
    }
}
