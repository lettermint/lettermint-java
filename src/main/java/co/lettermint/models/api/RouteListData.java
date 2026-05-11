package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteListData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("slug")
    public String slug;

    @JsonProperty("name")
    public String name;

    @JsonProperty("route_type")
    public RouteType routeType;

    @JsonProperty("is_default")
    public Boolean isDefault;

    @JsonProperty("webhooks_count")
    public Integer webhooksCount;

    @JsonProperty("suppressed_recipients_count")
    public Integer suppressedRecipientsCount;

    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("updated_at")
    public String updatedAt;
}
