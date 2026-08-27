package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RescheduleMessageResponse {
    @JsonProperty("message_id")
    public String messageId;

    @JsonProperty("status")
    public String status;

    @JsonProperty("scheduled_at")
    public String scheduledAt;
}
