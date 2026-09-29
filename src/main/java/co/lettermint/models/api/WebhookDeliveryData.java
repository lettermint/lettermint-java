package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookDeliveryData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("webhook_id")
    public String webhookId;

    @JsonProperty("event_type")
    public String eventType;

    @JsonProperty("source_scope")
    public String sourceScope;

    @JsonProperty("source_project_id")
    public String sourceProjectId;

    @JsonProperty("source_route_id")
    public String sourceRouteId;

    @JsonProperty("status")
    public String status;

    @JsonProperty("attempt_number")
    public Integer attemptNumber;

    @JsonProperty("http_status_code")
    public Integer httpStatusCode;

    @JsonProperty("duration_ms")
    public Integer durationMs;

    @JsonProperty("payload")
    public List<String> payload;

    @JsonProperty("response_body")
    public String responseBody;

    @JsonProperty("response_headers")
    public List<String> responseHeaders;

    @JsonProperty("error_message")
    public String errorMessage;

    @JsonProperty("delivered_at")
    public String deliveredAt;

    @JsonProperty("timestamp")
    public String timestamp;

    @JsonProperty("sandbox")
    public Boolean sandbox;
}
