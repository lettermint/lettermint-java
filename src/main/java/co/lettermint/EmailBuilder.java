package co.lettermint;

import co.lettermint.types.MessageTagInput;
import co.lettermint.types.SandboxResult;
import co.lettermint.types.SendMailRequestSettings;
import co.lettermint.types.SendMailResponse;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

/**
 * An immutable email builder bound to a client, created by {@code lettermint.emails().compose()}.
 *
 * <p>Every setter returns a new builder and leaves this one unchanged, so a base builder can be kept
 * and reused as a template, also across threads. A setter that throws leaves the builder unchanged.
 * Keep the returned builder when you build an email over several statements:
 *
 * <pre>{@code
 * EmailBuilder email = lettermint.emails().compose().from("hello@acme.com").to(user.email()).subject("Your invoice");
 * if (user.accountant() != null) {
 *     email = email.cc(user.accountant());
 * }
 * email.html(invoiceHtml).send();
 * }</pre>
 */
public final class EmailBuilder {
    private final Emails emails;
    private final EmailMessage message;

    EmailBuilder(Emails emails, EmailMessage message) {
        this.emails = emails;
        this.message = message;
    }

    /**
     * @param from the sender, for example {@code Acme <hello@acme.com>}
     * @return a new builder
     */
    public EmailBuilder from(String from) {
        return new EmailBuilder(emails, message.from(from));
    }

    /**
     * @param to the recipients; replaces the list
     * @return a new builder
     */
    public EmailBuilder to(String... to) {
        return new EmailBuilder(emails, message.to(to));
    }

    /**
     * @param to the recipients; replaces the list
     * @return a new builder
     */
    public EmailBuilder to(Collection<String> to) {
        return new EmailBuilder(emails, message.to(to));
    }

    /**
     * @param cc the CC recipients; replaces the list
     * @return a new builder
     */
    public EmailBuilder cc(String... cc) {
        return new EmailBuilder(emails, message.cc(cc));
    }

    /**
     * @param cc the CC recipients; replaces the list, null removes it
     * @return a new builder
     */
    public EmailBuilder cc(Collection<String> cc) {
        return new EmailBuilder(emails, message.cc(cc));
    }

    /**
     * @param bcc the BCC recipients; replaces the list
     * @return a new builder
     */
    public EmailBuilder bcc(String... bcc) {
        return new EmailBuilder(emails, message.bcc(bcc));
    }

    /**
     * @param bcc the BCC recipients; replaces the list, null removes it
     * @return a new builder
     */
    public EmailBuilder bcc(Collection<String> bcc) {
        return new EmailBuilder(emails, message.bcc(bcc));
    }

    /**
     * @param replyTo the Reply-To addresses; replaces the list
     * @return a new builder
     */
    public EmailBuilder replyTo(String... replyTo) {
        return new EmailBuilder(emails, message.replyTo(replyTo));
    }

    /**
     * @param replyTo the Reply-To addresses; replaces the list, null removes it
     * @return a new builder
     */
    public EmailBuilder replyTo(Collection<String> replyTo) {
        return new EmailBuilder(emails, message.replyTo(replyTo));
    }

    /**
     * @param subject the subject line
     * @return a new builder
     */
    public EmailBuilder subject(String subject) {
        return new EmailBuilder(emails, message.subject(subject));
    }

    /**
     * @param html the HTML body; null removes it
     * @return a new builder
     */
    public EmailBuilder html(String html) {
        return new EmailBuilder(emails, message.html(html));
    }

    /**
     * @param text the plain-text body; null removes it
     * @return a new builder
     */
    public EmailBuilder text(String text) {
        return new EmailBuilder(emails, message.text(text));
    }

    /**
     * @param headers custom email headers; replaces them, null removes them
     * @return a new builder
     */
    public EmailBuilder headers(Map<String, String> headers) {
        return new EmailBuilder(emails, message.headers(headers));
    }

    /**
     * @param metadata data stored with the message, not added as headers
     * @return a new builder
     */
    public EmailBuilder metadata(Map<String, String> metadata) {
        return new EmailBuilder(emails, message.metadata(metadata));
    }

    /**
     * @param tag the legacy single tag; null removes it
     * @return a new builder
     */
    public EmailBuilder tag(String tag) {
        return new EmailBuilder(emails, message.tag(tag));
    }

    /**
     * @param tags name/value tags (see {@link EmailMessage#tags(MessageTagInput...)}); replaces them
     * @return a new builder
     */
    public EmailBuilder tags(MessageTagInput... tags) {
        return new EmailBuilder(emails, message.tags(tags));
    }

    /**
     * @param tags name/value tags; replaces them, null removes them
     * @return a new builder
     */
    public EmailBuilder tags(Collection<MessageTagInput> tags) {
        return new EmailBuilder(emails, message.tags(tags));
    }

    /**
     * @param route the slug of the route to send through
     * @return a new builder
     */
    public EmailBuilder route(String route) {
        return new EmailBuilder(emails, message.route(route));
    }

    /**
     * @param scheduledAt the delivery time: ISO 8601 or English such as {@code tomorrow 9am}; null removes it
     * @return a new builder
     */
    public EmailBuilder scheduledAt(String scheduledAt) {
        return new EmailBuilder(emails, message.scheduledAt(scheduledAt));
    }

    /**
     * @param scheduledAt the delivery time, sent as ISO 8601 in UTC
     * @return a new builder
     */
    public EmailBuilder scheduledAt(Instant scheduledAt) {
        return new EmailBuilder(emails, message.scheduledAt(scheduledAt));
    }

    /**
     * @param settings per-email settings that override the route settings
     * @return a new builder
     */
    public EmailBuilder settings(SendMailRequestSettings settings) {
        return new EmailBuilder(emails, message.settings(settings));
    }

    /**
     * @param sandboxResult the result a Sandbox project simulates for every recipient
     * @return a new builder
     */
    public EmailBuilder sandboxResult(SandboxResult sandboxResult) {
        return new EmailBuilder(emails, message.sandboxResult(sandboxResult));
    }

    /**
     * @param attachment the attachment to add
     * @return a new builder
     */
    public EmailBuilder attach(EmailAttachment attachment) {
        return new EmailBuilder(emails, message.attach(attachment));
    }

    /**
     * @return the email as an {@link EmailMessage}, for example for {@code emails().sendBatch()}
     */
    public EmailMessage build() {
        return message;
    }

    /**
     * Sends this email. The builder stays unchanged and can be sent again.
     *
     * @return the message id and status
     */
    public SendMailResponse send() {
        return emails.send(message, null);
    }

    /**
     * Sends this email with options, such as an idempotency key.
     *
     * @param options the idempotency key and timeout of this call, or null
     * @return the message id and status
     */
    public SendMailResponse send(SendOptions options) {
        return emails.send(message, options);
    }

    /** The email, without attachment content. Contains no credentials. */
    @Override
    public String toString() {
        return "EmailBuilder{" + message + "}";
    }
}
