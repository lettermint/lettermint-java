package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("smtp_enabled")
    public Boolean smtpEnabled;

    @JsonProperty("redact_email_content")
    public Boolean redactEmailContent;

    @JsonProperty("default_route_id")
    public String defaultRouteId;

    @JsonProperty("token_generated_at")
    public String tokenGeneratedAt;

    @JsonProperty("token_last_used_at")
    public String tokenLastUsedAt;

    @JsonProperty("token_last_used_ip")
    public String tokenLastUsedIp;

    @JsonProperty("routes")
    public List<RouteData> routes;

    @JsonProperty("routes_count")
    public Integer routesCount;

    @JsonProperty("domains")
    public List<DomainData> domains;

    @JsonProperty("domains_count")
    public Integer domainsCount;

    @JsonProperty("last_28_days")
    public MessageStatsData last28Days;

    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("updated_at")
    public String updatedAt;
}
