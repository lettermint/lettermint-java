package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateTeamMemberAssignmentData {
    @JsonProperty("role_id")
    public String roleId;

    @JsonProperty("project_access")
    public Map<String, Object> projectAccess;
}
