package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamMemberData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("email")
    public String email;

    @JsonProperty("role")
    public Map<String, Object> role;

    @JsonProperty("project_access")
    public TeamMemberProjectAccessData projectAccess;

    @JsonProperty("joined_at")
    public String joinedAt;
}
