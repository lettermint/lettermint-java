package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookDeliveryListData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("webhook_id")
    public String webhookId;

    @JsonProperty("event_type")
    public String eventType;

    @JsonProperty("status")
    public String status;

    @JsonProperty("attempt_number")
    public Integer attemptNumber;

    @JsonProperty("http_status_code")
    public Integer httpStatusCode;

    @JsonProperty("duration_ms")
    public Integer durationMs;

    @JsonProperty("delivered_at")
    public String deliveredAt;

    @JsonProperty("created_at")
    public String createdAt;
}
