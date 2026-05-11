package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SuppressedRecipientData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("type")
    public SuppressionType type;

    @JsonProperty("value")
    public String value;

    @JsonProperty("reason")
    public SuppressionReason reason;

    @JsonProperty("scope")
    public SuppressionScope scope;

    @JsonProperty("project_id")
    public String projectId;

    @JsonProperty("route_id")
    public String routeId;

    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("updated_at")
    public String updatedAt;
}
