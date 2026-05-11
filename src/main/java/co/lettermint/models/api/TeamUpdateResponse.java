package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamUpdateResponse {
    @JsonProperty("data")
    public TeamData data;

    @JsonProperty("message")
    public String message;
}
