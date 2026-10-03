package co.lettermint;

import co.lettermint.exceptions.LettermintValidationException;
import java.util.Base64;

/**
 * An attachment. Immutable: {@link #contentType(String)} and {@link #contentId(String)} return a copy.
 *
 * <pre>{@code
 * EmailAttachment.of("invoice.pdf", Files.readAllBytes(path)).contentType("application/pdf");
 * EmailAttachment.ofBase64("logo.png", logoBase64).contentId("logo");  // <img src="cid:logo">
 * }</pre>
 */
public final class EmailAttachment {
    private final String filename;
    private final byte[] bytes;
    private final String base64;
    private final String contentType;
    private final String contentId;

    private EmailAttachment(String filename, byte[] bytes, String base64, String contentType, String contentId) {
        this.filename = filename;
        this.bytes = bytes;
        this.base64 = base64;
        this.contentType = contentType;
        this.contentId = contentId;
    }

    private static String checkFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new LettermintValidationException("An attachment needs a filename.", "attachments");
        }
        return filename;
    }

    /**
     * @param filename the file name, for example {@code invoice.pdf}
     * @param content the raw bytes; the SDK copies them and base64-encodes them when sending
     * @return the attachment
     */
    public static EmailAttachment of(String filename, byte[] content) {
        checkFilename(filename);
        if (content == null) {
            throw new LettermintValidationException("Attachment content must not be null.", "attachments");
        }
        return new EmailAttachment(filename, content.clone(), null, null, null);
    }

    /**
     * @param filename the file name, for example {@code logo.png}
     * @param base64 the content, already base64-encoded
     * @return the attachment
     */
    public static EmailAttachment ofBase64(String filename, String base64) {
        checkFilename(filename);
        if (base64 == null) {
            throw new LettermintValidationException("Attachment content must not be null.", "attachments");
        }
        return new EmailAttachment(filename, null, base64, null, null);
    }

    /**
     * @param contentType the MIME type, for example {@code application/pdf}; detected by the API when null
     * @return a copy with this content type
     */
    public EmailAttachment contentType(String contentType) {
        return new EmailAttachment(filename, bytes, base64, contentType, contentId);
    }

    /**
     * @param contentId the Content-ID of an inline image referenced as {@code cid:<contentId>}, or null
     * @return a copy with this Content-ID
     */
    public EmailAttachment contentId(String contentId) {
        return new EmailAttachment(filename, bytes, base64, contentType, contentId);
    }

    /**
     * @return the file name
     */
    public String filename() {
        return filename;
    }

    /**
     * @return the MIME type, or null
     */
    public String contentType() {
        return contentType;
    }

    /**
     * @return the Content-ID, or null
     */
    public String contentId() {
        return contentId;
    }

    /**
     * @return the content, base64-encoded
     */
    public String base64Content() {
        return base64 != null ? base64 : Base64.getEncoder().encodeToString(bytes);
    }

    @Override
    public String toString() {
        return "EmailAttachment{filename=" + filename
                + (contentType != null ? ", contentType=" + contentType : "")
                + (contentId != null ? ", contentId=" + contentId : "") + "}";
    }
}
