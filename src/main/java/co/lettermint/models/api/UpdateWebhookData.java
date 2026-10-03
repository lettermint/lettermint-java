package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateWebhookData {
    @JsonProperty("name")
    public String name;

    @JsonProperty("url")
    public String url;

    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    @JsonProperty("basic_auth")
    public OptionalNullable<WebhookBasicAuthData> basicAuth;

    @JsonProperty("events")
    public List<String> events;

    @JsonProperty("enabled")
    public Boolean enabled;

    @JsonProperty("include_machine_events")
    public Boolean includeMachineEvents;

    @JsonProperty("scope")
    public String scope;

    @JsonProperty("project_ids")
    public List<String> projectIds;

    @JsonProperty("route_ids")
    public List<String> routeIds;

    @JsonProperty("route_id")
    public String routeId;

    @JsonProperty("delivery_mode_filter")
    public String deliveryModeFilter;
}
