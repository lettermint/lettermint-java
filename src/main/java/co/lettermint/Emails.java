package co.lettermint;

import co.lettermint.exceptions.LettermintValidationException;
import co.lettermint.types.Operations;
import co.lettermint.types.Operations.AuthSurface;
import co.lettermint.types.SendBatchMailItem;
import co.lettermint.types.SendMailResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sends email with the project sending token ({@code x-lettermint-token}). Holds no message state:
 * every call sends exactly what it is given. Thread-safe.
 */
public final class Emails {
    private final Transport transport;

    Emails(Transport transport) {
        this.transport = transport;
    }

    /**
     * Sends one email.
     *
     * @param message the email
     * @return the message id and status
     */
    public SendMailResponse send(EmailMessage message) {
        return send(message, null);
    }

    /**
     * Sends one email.
     *
     * @param message the email
     * @param options the idempotency key and timeout of this call, or null
     * @return the message id and status
     */
    public SendMailResponse send(EmailMessage message, SendOptions options) {
        transport.assertAuth("emails.send", AuthSurface.SENDING);
        if (message == null) {
            throw new LettermintValidationException("send() takes an EmailMessage.", "message");
        }
        EmailValidation.validate(message, "");
        if (message.idempotencyKey() != null) {
            throw new LettermintValidationException("A per-email idempotencyKey applies only to sendBatch(); pass SendOptions.idempotencyKey(...) to send().",
                    "idempotencyKey");
        }
        return transport.call(Operations.SEND_MAIL, Transport.call("emails.send").body(message.toRequest()).options(options));
    }

    /**
     * Sends up to 500 emails in one request. Use {@link EmailBuilder#build()} for builders. Give each
     * email an {@link EmailMessage#idempotencyKey(String)} to make it safe to send again, in this or a
     * later batch.
     *
     * @param messages the emails
     * @return one result per email, in order
     */
    public List<SendMailResponse> sendBatch(List<EmailMessage> messages) {
        return sendBatch(messages, null);
    }

    /**
     * Sends up to 500 emails in one request.
     *
     * <p>{@code options} can carry an idempotency key for the whole request ({@code Idempotency-Key}
     * header); per-email keys are set with {@link EmailMessage#idempotencyKey(String)} and must be unique
     * within the batch. If the batch fails part-way, emails before the failing one may already be
     * accepted: {@link co.lettermint.exceptions.ApiException#getDetails()} then has an {@code accepted}
     * list with the {@code index} and {@code message_id} of each.
     *
     * @param messages the emails
     * @param options the idempotency key and timeout of this call, or null
     * @return one result per email, in order
     */
    public List<SendMailResponse> sendBatch(List<EmailMessage> messages, SendOptions options) {
        transport.assertAuth("emails.sendBatch", AuthSurface.SENDING);
        if (messages == null) {
            throw new LettermintValidationException("sendBatch() takes a list of messages.", "messages");
        }
        List<SendBatchMailItem> body = new ArrayList<>(messages.size());
        Set<String> keys = new HashSet<>();
        for (int index = 0; index < messages.size(); index++) {
            EmailMessage message = messages.get(index);
            if (message == null) {
                throw new LettermintValidationException("An email message must not be null.", "messages[" + index + "]");
            }
            EmailValidation.validate(message, "messages[" + index + "]");
            if (message.idempotencyKey() != null && !keys.add(message.idempotencyKey())) {
                throw new LettermintValidationException("Idempotency keys must be unique within a batch.", "messages[" + index + "].idempotencyKey");
            }
            body.add(message.toBatchItem());
        }
        return transport.call(Operations.SEND_BATCH_MAIL, Transport.call("emails.sendBatch").body(body).options(options));
    }

    /**
     * Starts an immutable email builder. Every setter returns a new builder.
     *
     * @return an empty builder bound to this client
     */
    public EmailBuilder compose() {
        return compose(EmailMessage.create());
    }

    /**
     * Starts an immutable email builder from a message.
     *
     * @param message the message to start from
     * @return a builder bound to this client
     */
    public EmailBuilder compose(EmailMessage message) {
        transport.assertAuth("emails.compose", AuthSurface.SENDING);
        EmailValidation.validate(message == null ? EmailMessage.create() : message, "");
        return new EmailBuilder(this, message == null ? EmailMessage.create() : message);
    }

    /**
     * Checks the sending token: {@code GET /ping} returns {@code pong}.
     *
     * @return {@code pong}
     */
    public String ping() {
        return ping(null);
    }

    /**
     * @param options per-call options, or null
     * @return {@code pong}
     * @see #ping()
     */
    public String ping(RequestOptions options) {
        return transport.call(Operations.PING, Transport.call("emails.ping").auth(AuthSurface.SENDING).options(options)).trim();
    }

    @Override
    public String toString() {
        return "Emails{}";
    }
}
