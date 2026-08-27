package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SendMailRequest {
    @JsonProperty("route")
    public String route;

    @JsonProperty("from")
    public String fromValue;

    @JsonProperty("to")
    public List<String> to;

    @JsonProperty("cc")
    public List<String> cc;

    @JsonProperty("bcc")
    public List<String> bcc;

    @JsonProperty("reply_to")
    public List<String> replyTo;

    @JsonProperty("subject")
    public String subject;

    @JsonProperty("scheduled_at")
    public String scheduledAt;

    @JsonProperty("headers")
    public Map<String, String> headers;

    @JsonProperty("metadata")
    public Map<String, String> metadata;

    @JsonProperty("tag")
    public String tag;

    @JsonProperty("tags")
    public List<Map<String, Object>> tags;

    @JsonProperty("settings")
    public Map<String, Object> settings;

    @JsonProperty("html")
    public String html;

    @JsonProperty("text")
    public String text;

    @JsonProperty("attachments")
    public List<Map<String, Object>> attachments;
}
