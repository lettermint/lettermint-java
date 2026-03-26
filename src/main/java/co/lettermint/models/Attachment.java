package co.lettermint.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an email attachment.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attachment {

    @JsonProperty("filename")
    private final String filename;

    @JsonProperty("content")
    private final String content;

    @JsonProperty("content_id")
    private final String contentId;

    @JsonProperty("content_type")
    private final String contentType;

    public Attachment(String filename, String content) {
        this(filename, content, null, null);
    }

    public Attachment(String filename, String content, String contentId, String contentType) {
        this.filename = filename;
        this.content = content;
        this.contentId = contentId;
        this.contentType = contentType;
    }

    public String getFilename() {
        return filename;
    }

    public String getContent() {
        return content;
    }

    public String getContentId() {
        return contentId;
    }

    public String getContentType() {
        return contentType;
    }
}
