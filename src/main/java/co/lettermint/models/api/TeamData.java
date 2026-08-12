package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("type")
    public String type;

    @JsonProperty("plan")
    public String plan;

    @JsonProperty("included_volume")
    public Integer includedVolume;

    @JsonProperty("tier")
    public Integer tier;

    @JsonProperty("verified_at")
    public String verifiedAt;

    @JsonProperty("features")
    public List<String> features;

    @JsonProperty("addons")
    public List<TeamAddonData> addons;

    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("domains_count")
    public Integer domainsCount;

    @JsonProperty("projects_count")
    public Integer projectsCount;

    @JsonProperty("members_count")
    public Integer membersCount;
}
