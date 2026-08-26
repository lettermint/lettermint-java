package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("type")
    public String type;

    @JsonProperty("status")
    public String status;

    @JsonProperty("status_changed_at")
    public String statusChangedAt;

    @JsonProperty("scheduled_at")
    public String scheduledAt;

    @JsonProperty("tag")
    public String tag;

    @JsonProperty("tags")
    public List<Map<String, Object>> tags;

    @JsonProperty("from_email")
    public String fromEmail;

    @JsonProperty("from_name")
    public String fromName;

    @JsonProperty("reply_to")
    public List<String> replyTo;

    @JsonProperty("subject")
    public String subject;

    @JsonProperty("to")
    public List<MessageRecipientData> to;

    @JsonProperty("cc")
    public List<MessageRecipientData> cc;

    @JsonProperty("bcc")
    public List<MessageRecipientData> bcc;

    @JsonProperty("attachments")
    public List<MessageAttachmentData> attachments;

    @JsonProperty("metadata")
    public Map<String, String> metadata;

    @JsonProperty("spam_score")
    public Double spamScore;

    @JsonProperty("spam_symbols")
    public List<SpamSymbol> spamSymbols;

    @JsonProperty("route_id")
    public String routeId;

    @JsonProperty("created_at")
    public String createdAt;
}
