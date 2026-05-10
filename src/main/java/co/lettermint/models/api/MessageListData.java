package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageListData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("type")
    public MessageType type;

    @JsonProperty("status")
    public MessageStatus status;

    @JsonProperty("from_email")
    public String fromEmail;

    @JsonProperty("from_name")
    public String fromName;

    @JsonProperty("subject")
    public String subject;

    @JsonProperty("to")
    public List<MessageRecipientData> to;

    @JsonProperty("cc")
    public List<MessageRecipientData> cc;

    @JsonProperty("bcc")
    public List<MessageRecipientData> bcc;

    @JsonProperty("reply_to")
    public List<String> replyTo;

    @JsonProperty("tag")
    public String tag;

    @JsonProperty("created_at")
    public String createdAt;
}
