package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatsRequestData {
    @JsonProperty("from")
    public String fromValue;

    @JsonProperty("to")
    public String to;

    @JsonProperty("project_id")
    public String projectId;

    @JsonProperty("include_machine")
    public Boolean includeMachine;
}
