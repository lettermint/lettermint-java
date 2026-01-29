package co.lettermint.exceptions.webhook;

import co.lettermint.exceptions.LettermintException;

/**
 * Base exception for webhook verification failures.
 */
public class WebhookVerificationException extends LettermintException {

    public WebhookVerificationException(String message) {
        super(message);
    }

    public WebhookVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
