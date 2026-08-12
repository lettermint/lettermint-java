package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SuppressionDestroyResponse {
    @JsonProperty("success")
    public Boolean success;

    @JsonProperty("status")
    public String status;

    @JsonProperty("message")
    public String message;

    @JsonProperty("confidence")
    public Double confidence;
}
