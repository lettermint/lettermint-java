package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteUpdateResponse {
    @JsonProperty("data")
    public RouteData data;

    @JsonProperty("message")
    public String message;
}
