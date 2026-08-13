package co.lettermint.endpoints;

import co.lettermint.client.LettermintClient;
import co.lettermint.models.Attachment;
import co.lettermint.models.SendEmailResponse;
import co.lettermint.models.api.SendMailRequest;
import co.lettermint.models.api.SendMailResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;

/**
 * Fluent builder for sending emails via the Lettermint API.
 */
public class EmailEndpoint extends Endpoint {

    private String from;
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private List<String> replyTo;
    private String subject;
    private String html;
    private String text;
    private Map<String, String> headers;
    private List<Attachment> attachments;
    private String route;
    private Map<String, Object> metadata;
    private String tag;
    private Map<String, Object> settings;
    private String idempotencyKey;

    public EmailEndpoint(LettermintClient client) {
        super(client);
        reset();
    }

    /**
     * Reset all fields to initial state.
     */
    private void reset() {
        this.from = null;
        this.to = new ArrayList<>();
        this.cc = new ArrayList<>();
        this.bcc = new ArrayList<>();
        this.replyTo = new ArrayList<>();
        this.subject = null;
        this.html = null;
        this.text = null;
        this.headers = new LinkedHashMap<>();
        this.attachments = new ArrayList<>();
        this.route = null;
        this.metadata = new LinkedHashMap<>();
        this.tag = null;
        this.settings = null;
        this.idempotencyKey = null;
    }

    /**
     * Set the sender email address.
     * Format: {@code "Name <email>"} or just {@code "email"}
     */
    public EmailEndpoint from(String from) {
        this.from = from;
        return this;
    }

    /**
     * Set recipient email addresses. Replaces any existing recipients.
     */
    public EmailEndpoint to(String... emails) {
        this.to = new ArrayList<>(Arrays.asList(emails));
        return this;
    }

    /**
     * Set CC email addresses. Replaces any existing CC recipients.
     */
    public EmailEndpoint cc(String... emails) {
        this.cc = new ArrayList<>(Arrays.asList(emails));
        return this;
    }

    /**
     * Set BCC email addresses. Replaces any existing BCC recipients.
     */
    public EmailEndpoint bcc(String... emails) {
        this.bcc = new ArrayList<>(Arrays.asList(emails));
        return this;
    }

    /**
     * Set reply-to email addresses. Replaces any existing reply-to addresses.
     */
    public EmailEndpoint replyTo(String... emails) {
        this.replyTo = new ArrayList<>(Arrays.asList(emails));
        return this;
    }

    /**
     * Set the email subject.
     */
    public EmailEndpoint subject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Set the HTML content of the email.
     */
    public EmailEndpoint html(String html) {
        this.html = html;
        return this;
    }

    /**
     * Set the plain text content of the email.
     */
    public EmailEndpoint text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Set custom email headers.
     */
    public EmailEndpoint headers(Map<String, String> headers) {
        this.headers = new LinkedHashMap<>(headers);
        return this;
    }

    /**
     * Add a single custom header.
     */
    public EmailEndpoint header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * Attach a file to the email.
     *
     * @param filename The filename to use for the attachment
     * @param content  Base64-encoded file content
     */
    public EmailEndpoint attach(String filename, String content) {
        this.attachments.add(new Attachment(filename, content));
        return this;
    }

    /**
     * Attach an inline file to the email.
     *
     * @param filename  The filename to use for the attachment
     * @param content   Base64-encoded file content
     * @param contentId Content-ID for inline attachments (e.g., for embedding images)
     */
    public EmailEndpoint attach(String filename, String content, String contentId) {
        this.attachments.add(new Attachment(filename, content, contentId, null));
        return this;
    }

    /**
     * Attach an inline file to the email and specify the {@code Content-Type}
     *
     * @param filename    The filename to use for the attachment
     * @param content     Base64-encoded file content
     * @param contentId   Content-ID for inline attachments (e.g., for embedding images)
     * @param contentType MIME type of the attachment (e.g., "image/png")
     */
    public EmailEndpoint attach(String filename, String content, String contentId, String contentType) {
        this.attachments.add(new Attachment(filename, content, contentId, contentType));
        return this;
    }

    /**
     * Set the route slug for sending through a specific route.
     */
    public EmailEndpoint route(String route) {
        this.route = route;
        return this;
    }

    /**
     * Set metadata to attach to the email.
     */
    public EmailEndpoint metadata(Map<String, Object> metadata) {
        this.metadata = new LinkedHashMap<>(metadata);
        return this;
    }

    /**
     * Add a single metadata key-value pair.
     */
    public EmailEndpoint metadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Set the email tag.
     */
    public EmailEndpoint tag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * Set per-email settings that override the selected route.
     */
    public EmailEndpoint settings(Map<String, Object> settings) {
        this.settings = new LinkedHashMap<>(settings);
        return this;
    }

    /**
     * Set an idempotency key for the request.
     */
    public EmailEndpoint idempotencyKey(String key) {
        this.idempotencyKey = key;
        return this;
    }

    /**
     * Send the email and return the response.
     * Clears all fields after sending (matches PHP SDK behavior).
     */
    public SendEmailResponse send() {
        Map<String, Object> payload = buildPayload();
        Map<String, String> requestHeaders = null;

        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            requestHeaders = Collections.singletonMap("Idempotency-Key", idempotencyKey);
        }

        try {
            return client.post("/send", payload, SendEmailResponse.class, requestHeaders);
        } finally {
            reset();
        }
    }

    public List<SendMailResponse> sendBatch(List<SendMailRequest> payloads) {
        Map<String, String> requestHeaders = null;

        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            requestHeaders = Collections.singletonMap("Idempotency-Key", idempotencyKey);
        }

        try {
            return client.post("/send/batch", payloads, new TypeReference<List<SendMailResponse>>() {}, requestHeaders);
        } finally {
            reset();
        }
    }

    public String ping() {
        return client.getRaw("/ping").trim();
    }

    private Map<String, Object> buildPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();

        if (from != null) {
            payload.put("from", from);
        }

        if (!to.isEmpty()) {
            payload.put("to", to);
        }

        if (!cc.isEmpty()) {
            payload.put("cc", cc);
        }

        if (!bcc.isEmpty()) {
            payload.put("bcc", bcc);
        }

        if (!replyTo.isEmpty()) {
            payload.put("reply_to", replyTo);
        }

        if (subject != null) {
            payload.put("subject", subject);
        }

        if (html != null) {
            payload.put("html", html);
        }

        if (text != null) {
            payload.put("text", text);
        }

        if (!headers.isEmpty()) {
            payload.put("headers", headers);
        }

        if (!attachments.isEmpty()) {
            payload.put("attachments", attachments);
        }

        if (route != null) {
            payload.put("route", route);
        }

        if (!metadata.isEmpty()) {
            payload.put("metadata", metadata);
        }

        if (tag != null) {
            payload.put("tag", tag);
        }

        if (settings != null) {
            payload.put("settings", settings);
        }

        return payload;
    }
}
