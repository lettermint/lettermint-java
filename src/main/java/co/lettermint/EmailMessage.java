package co.lettermint;

import co.lettermint.types.MessageAttachmentInput;
import co.lettermint.types.MessageTagInput;
import co.lettermint.types.SandboxResult;
import co.lettermint.types.SendMailRequest;
import co.lettermint.types.SendMailRequestSettings;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * An email. Immutable and thread-safe: every setter returns a new message and leaves this one
 * unchanged, so a message can be shared and reused as a template. A setter that throws leaves the
 * message unchanged.
 *
 * <pre>{@code
 * EmailMessage message = EmailMessage.create()
 *     .from("Acme <hello@acme.com>")
 *     .to("jane@example.com")
 *     .subject("Welcome to Acme")
 *     .html("<p>Thanks for signing up.</p>");
 * lettermint.emails().send(message);
 * }</pre>
 *
 * <p>Each setter has an accessor of the same name without arguments: {@code message.subject()}.
 * {@code to}, {@code cc}, {@code bcc} and {@code replyTo} replace their list; {@link #attach} appends.
 */
public final class EmailMessage {
    private static final EmailMessage EMPTY = new EmailMessage(new Fields());

    /** The values; every collection is unmodifiable, so a shallow copy is a full copy. */
    private static final class Fields implements Cloneable {
        String from;
        List<String> to = List.of();
        List<String> cc;
        List<String> bcc;
        List<String> replyTo;
        String subject;
        String html;
        String text;
        Map<String, String> headers;
        Map<String, String> metadata;
        String tag;
        List<MessageTagInput> tags;
        String route;
        String scheduledAt;
        SendMailRequestSettings settings;
        SandboxResult sandboxResult;
        List<EmailAttachment> attachments;

        @Override
        protected Fields clone() {
            try {
                return (Fields) super.clone();
            } catch (CloneNotSupportedException error) {
                throw new AssertionError(error);
            }
        }
    }

    @FunctionalInterface
    private interface Change {
        void apply(Fields fields);
    }

    private final Fields fields;

    private EmailMessage(Fields fields) {
        this.fields = fields;
    }

    /**
     * @return an empty message
     */
    public static EmailMessage create() {
        return EMPTY;
    }

    private EmailMessage with(Change change) {
        Fields copy = fields.clone();
        change.apply(copy);
        EmailMessage message = new EmailMessage(copy);
        EmailValidation.validate(message, "");
        return message;
    }

    private static List<String> list(Collection<String> values) {
        return values == null ? null : Collections.unmodifiableList(new ArrayList<>(values));
    }

    private static List<String> list(String[] values) {
        return values == null ? null : list(Arrays.asList(values));
    }

    private static Map<String, String> map(Map<String, String> values) {
        return values == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    /**
     * @param from the sender, for example {@code Acme <hello@acme.com>}
     * @return a copy with this sender
     */
    public EmailMessage from(String from) {
        return with(f -> f.from = from);
    }

    /**
     * @param to the recipients; replaces the list
     * @return a copy with these recipients
     */
    public EmailMessage to(String... to) {
        List<String> value = list(to);
        return with(f -> f.to = value == null ? List.of() : value);
    }

    /**
     * @param to the recipients; replaces the list
     * @return a copy with these recipients
     */
    public EmailMessage to(Collection<String> to) {
        List<String> value = list(to);
        return with(f -> f.to = value == null ? List.of() : value);
    }

    /**
     * @param cc the CC recipients; replaces the list
     * @return a copy with these CC recipients
     */
    public EmailMessage cc(String... cc) {
        List<String> value = list(cc);
        return with(f -> f.cc = value);
    }

    /**
     * @param cc the CC recipients; replaces the list, null removes it
     * @return a copy with these CC recipients
     */
    public EmailMessage cc(Collection<String> cc) {
        List<String> value = list(cc);
        return with(f -> f.cc = value);
    }

    /**
     * @param bcc the BCC recipients; replaces the list
     * @return a copy with these BCC recipients
     */
    public EmailMessage bcc(String... bcc) {
        List<String> value = list(bcc);
        return with(f -> f.bcc = value);
    }

    /**
     * @param bcc the BCC recipients; replaces the list, null removes it
     * @return a copy with these BCC recipients
     */
    public EmailMessage bcc(Collection<String> bcc) {
        List<String> value = list(bcc);
        return with(f -> f.bcc = value);
    }

    /**
     * @param replyTo the Reply-To addresses; replaces the list
     * @return a copy with these Reply-To addresses
     */
    public EmailMessage replyTo(String... replyTo) {
        List<String> value = list(replyTo);
        return with(f -> f.replyTo = value);
    }

    /**
     * @param replyTo the Reply-To addresses; replaces the list, null removes it
     * @return a copy with these Reply-To addresses
     */
    public EmailMessage replyTo(Collection<String> replyTo) {
        List<String> value = list(replyTo);
        return with(f -> f.replyTo = value);
    }

    /**
     * @param subject the subject line
     * @return a copy with this subject
     */
    public EmailMessage subject(String subject) {
        return with(f -> f.subject = subject);
    }

    /**
     * @param html the HTML body; null removes it
     * @return a copy with this HTML body
     */
    public EmailMessage html(String html) {
        return with(f -> f.html = html);
    }

    /**
     * @param text the plain-text body; null removes it
     * @return a copy with this text body
     */
    public EmailMessage text(String text) {
        return with(f -> f.text = text);
    }

    /**
     * @param headers custom email headers; replaces them, null removes them
     * @return a copy with these headers
     */
    public EmailMessage headers(Map<String, String> headers) {
        Map<String, String> value = map(headers);
        return with(f -> f.headers = value);
    }

    /**
     * @param metadata data stored with the message, not added as headers; replaces it, null removes it
     * @return a copy with this metadata
     */
    public EmailMessage metadata(Map<String, String> metadata) {
        Map<String, String> value = map(metadata);
        return with(f -> f.metadata = value);
    }

    /**
     * @param tag the legacy single tag; null removes it
     * @return a copy with this tag
     */
    public EmailMessage tag(String tag) {
        return with(f -> f.tag = tag);
    }

    /**
     * Name/value tags: up to 20, or 19 with a legacy {@link #tag(String)}. Names match
     * {@code ^[A-Za-z0-9_-]{1,32}$}, do not start with {@code __lettermint} and are unique; values match
     * {@code ^[A-Za-z0-9_-]{1,64}$}.
     *
     * @param tags the tags, for example {@code new MessageTagInput("campaign", "welcome")}; replaces them
     * @return a copy with these tags
     * @throws co.lettermint.exceptions.LettermintValidationException when a tag is invalid
     */
    public EmailMessage tags(MessageTagInput... tags) {
        return tags(tags == null ? null : Arrays.asList(tags));
    }

    /**
     * @param tags the tags; replaces them, null removes them
     * @return a copy with these tags
     * @see #tags(MessageTagInput...)
     */
    public EmailMessage tags(Collection<MessageTagInput> tags) {
        List<MessageTagInput> value = tags == null ? null : Collections.unmodifiableList(new ArrayList<>(tags));
        return with(f -> f.tags = value);
    }

    /**
     * @param route the slug of the route to send through; null removes it
     * @return a copy with this route
     */
    public EmailMessage route(String route) {
        return with(f -> f.route = route);
    }

    /**
     * @param scheduledAt the delivery time: ISO 8601, or English such as {@code tomorrow 9am}; null
     *     removes it
     * @return a copy with this delivery time
     */
    public EmailMessage scheduledAt(String scheduledAt) {
        return with(f -> f.scheduledAt = scheduledAt);
    }

    /**
     * @param scheduledAt the delivery time, sent as ISO 8601 in UTC
     * @return a copy with this delivery time
     */
    public EmailMessage scheduledAt(Instant scheduledAt) {
        return scheduledAt(scheduledAt == null ? null : scheduledAt.toString());
    }

    /**
     * @param settings per-email settings that override the route settings; null removes them
     * @return a copy with these settings
     */
    public EmailMessage settings(SendMailRequestSettings settings) {
        return with(f -> f.settings = settings);
    }

    /**
     * @param sandboxResult the result a Sandbox project simulates for every recipient; null removes it
     * @return a copy with this result
     */
    public EmailMessage sandboxResult(SandboxResult sandboxResult) {
        return with(f -> f.sandboxResult = sandboxResult);
    }

    /**
     * @param attachment the attachment to add
     * @return a copy with the attachment appended
     */
    public EmailMessage attach(EmailAttachment attachment) {
        Objects.requireNonNull(attachment, "attachment");
        return with(f -> {
            List<EmailAttachment> next = new ArrayList<>(f.attachments == null ? List.of() : f.attachments);
            next.add(attachment);
            f.attachments = Collections.unmodifiableList(next);
        });
    }

    /** @return the sender, or null */
    public String from() {
        return fields.from;
    }

    /** @return the recipients (unmodifiable, possibly empty) */
    public List<String> to() {
        return fields.to;
    }

    /** @return the CC recipients, or null */
    public List<String> cc() {
        return fields.cc;
    }

    /** @return the BCC recipients, or null */
    public List<String> bcc() {
        return fields.bcc;
    }

    /** @return the Reply-To addresses, or null */
    public List<String> replyTo() {
        return fields.replyTo;
    }

    /** @return the subject, or null */
    public String subject() {
        return fields.subject;
    }

    /** @return the HTML body, or null */
    public String html() {
        return fields.html;
    }

    /** @return the plain-text body, or null */
    public String text() {
        return fields.text;
    }

    /** @return the custom headers, or null */
    public Map<String, String> headers() {
        return fields.headers;
    }

    /** @return the metadata, or null */
    public Map<String, String> metadata() {
        return fields.metadata;
    }

    /** @return the legacy tag, or null */
    public String tag() {
        return fields.tag;
    }

    /** @return the name/value tags, or null */
    public List<MessageTagInput> tags() {
        return fields.tags;
    }

    /** @return the route slug, or null */
    public String route() {
        return fields.route;
    }

    /** @return the delivery time, or null */
    public String scheduledAt() {
        return fields.scheduledAt;
    }

    /** @return the per-email settings, or null */
    public SendMailRequestSettings settings() {
        return fields.settings;
    }

    /** @return the simulated Sandbox result, or null */
    public SandboxResult sandboxResult() {
        return fields.sandboxResult;
    }

    /** @return the attachments (unmodifiable, possibly empty) */
    public List<EmailAttachment> attachments() {
        return fields.attachments == null ? List.of() : fields.attachments;
    }

    /**
     * @return the message in the API's wire format, with attachments base64-encoded
     */
    public SendMailRequest toRequest() {
        SendMailRequest.Builder request = SendMailRequest.builder()
                .from(fields.from)
                .to(fields.to)
                .cc(fields.cc)
                .bcc(fields.bcc)
                .replyTo(fields.replyTo)
                .subject(fields.subject)
                .headers(fields.headers)
                .metadata(fields.metadata)
                .tags(fields.tags)
                .route(fields.route)
                .scheduledAt(fields.scheduledAt)
                .settings(fields.settings)
                .sandboxResult(fields.sandboxResult);
        // Optional and nullable on the wire: set only when present, so that nothing is sent as null.
        if (fields.html != null) {
            request.html(fields.html);
        }
        if (fields.text != null) {
            request.text(fields.text);
        }
        if (fields.tag != null) {
            request.tag(fields.tag);
        }
        if (fields.attachments != null) {
            List<MessageAttachmentInput> attachments = new ArrayList<>();
            for (EmailAttachment attachment : fields.attachments) {
                MessageAttachmentInput.Builder input = MessageAttachmentInput.builder()
                        .filename(attachment.filename())
                        .content(attachment.base64Content());
                if (attachment.contentType() != null) {
                    input.contentType(attachment.contentType());
                }
                if (attachment.contentId() != null) {
                    input.contentId(attachment.contentId());
                }
                attachments.add(input.build());
            }
            request.attachments(attachments);
        }
        return request.build();
    }

    /** The message without attachment content. Contains no credentials. */
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "EmailMessage{", "}");
        add(joiner, "from", fields.from);
        add(joiner, "to", fields.to.isEmpty() ? null : fields.to);
        add(joiner, "cc", fields.cc);
        add(joiner, "bcc", fields.bcc);
        add(joiner, "replyTo", fields.replyTo);
        add(joiner, "subject", fields.subject);
        add(joiner, "html", fields.html == null ? null : "(" + fields.html.length() + " characters)");
        add(joiner, "text", fields.text == null ? null : "(" + fields.text.length() + " characters)");
        add(joiner, "headers", fields.headers);
        add(joiner, "metadata", fields.metadata);
        add(joiner, "tag", fields.tag);
        add(joiner, "tags", fields.tags);
        add(joiner, "route", fields.route);
        add(joiner, "scheduledAt", fields.scheduledAt);
        add(joiner, "settings", fields.settings);
        add(joiner, "sandboxResult", fields.sandboxResult);
        add(joiner, "attachments", fields.attachments);
        return joiner.toString();
    }

    private static void add(StringJoiner joiner, String name, Object value) {
        if (value != null) {
            joiner.add(name + "=" + value);
        }
    }
}
