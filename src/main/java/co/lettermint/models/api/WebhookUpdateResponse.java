package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookUpdateResponse {
    @JsonProperty("data")
    public WebhookData data;

    @JsonProperty("message")
    public String message;
}
