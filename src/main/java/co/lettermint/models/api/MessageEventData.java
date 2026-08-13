package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageEventData {
    @JsonProperty("message_id")
    public String messageId;

    @JsonProperty("event")
    public String event;

    @JsonProperty("metadata")
    public Map<String, Object> metadata;

    @JsonProperty("timestamp")
    public String timestamp;
}
