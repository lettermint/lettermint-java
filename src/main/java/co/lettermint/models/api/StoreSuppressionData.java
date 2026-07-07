package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreSuppressionData {
    @JsonProperty("email")
    public String email;

    @JsonProperty("reason")
    public SuppressionReason reason;

    @JsonProperty("scope")
    public SuppressionScope scope;

    @JsonProperty("route_id")
    public String routeId;

    @JsonProperty("project_id")
    public String projectId;

    @JsonProperty("emails")
    public List<String> emails;
}
