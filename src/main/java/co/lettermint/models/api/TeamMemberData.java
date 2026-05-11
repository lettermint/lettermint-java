package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamMemberData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("user")
    public UserData user;

    @JsonProperty("role")
    public String role;

    @JsonProperty("joined_at")
    public String joinedAt;
}
