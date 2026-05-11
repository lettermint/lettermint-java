package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectUpdateResponse {
    @JsonProperty("data")
    public ProjectData data;

    @JsonProperty("message")
    public String message;
}
