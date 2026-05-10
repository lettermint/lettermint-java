package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("route_id")
    public String routeId;

    @JsonProperty("name")
    public String name;

    @JsonProperty("url")
    public String url;

    @JsonProperty("events")
    public List<String> events;

    @JsonProperty("enabled")
    public Boolean enabled;

    @JsonProperty("include_machine_events")
    public Boolean includeMachineEvents;

    @JsonProperty("secret")
    public String secret;

    @JsonProperty("last_called_at")
    public String lastCalledAt;

    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("updated_at")
    public String updatedAt;
}
