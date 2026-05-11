package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookTestResponse {
    @JsonProperty("message")
    public String message;

    @JsonProperty("delivery_id")
    public String deliveryId;
}
