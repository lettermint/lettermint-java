package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamRoleData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("system_key")
    public String systemKey;

    @JsonProperty("permissions")
    public List<String> permissions;

    @JsonProperty("assignable")
    public Boolean assignable;
}
