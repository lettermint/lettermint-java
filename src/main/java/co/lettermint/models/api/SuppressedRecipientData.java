package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SuppressedRecipientData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("type")
    public String type;

    @JsonProperty("value")
    public String value;

    @JsonProperty("reason")
    public String reason;

    @JsonProperty("scope")
    public String scope;

    @JsonProperty("project_id")
    public String projectId;

    @JsonProperty("route_id")
    public String routeId;

    @JsonProperty("source_message")
    public SuppressionSourceMessageData sourceMessage;

    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("updated_at")
    public String updatedAt;
}
