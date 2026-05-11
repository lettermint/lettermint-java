package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectRotateTokenResponse {
    @JsonProperty("data")
    public ProjectData data;

    @JsonProperty("new_token")
    public String newToken;

    @JsonProperty("message")
    public String message;
}
