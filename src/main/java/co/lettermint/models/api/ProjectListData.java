package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectListData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("smtp_enabled")
    public Boolean smtpEnabled;

    @JsonProperty("routes_count")
    public Integer routesCount;

    @JsonProperty("domains_count")
    public Integer domainsCount;

    @JsonProperty("team_members_count")
    public Integer teamMembersCount;

    @JsonProperty("last_28_days")
    public MessageStatsData last28Days;

    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("updated_at")
    public String updatedAt;
}
