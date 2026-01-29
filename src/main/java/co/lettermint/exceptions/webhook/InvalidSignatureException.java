package co.lettermint.exceptions.webhook;

/**
 * Exception thrown when webhook signature validation fails.
 */
public class InvalidSignatureException extends WebhookVerificationException {

    public InvalidSignatureException(String message) {
        super(message);
    }

    public InvalidSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
