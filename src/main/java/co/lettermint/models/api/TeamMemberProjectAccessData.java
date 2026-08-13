package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamMemberProjectAccessData {
    @JsonProperty("scope")
    public String scope;

    @JsonProperty("projects")
    public List<Map<String, Object>> projects;
}
