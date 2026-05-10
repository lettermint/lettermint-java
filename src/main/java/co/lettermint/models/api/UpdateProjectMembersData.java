package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProjectMembersData {
    @JsonProperty("team_member_ids")
    public List<String> teamMemberIds;
}
