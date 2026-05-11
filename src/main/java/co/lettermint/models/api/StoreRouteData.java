package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreRouteData {
    @JsonProperty("name")
    public String name;

    @JsonProperty("route_type")
    public RouteType routeType;

    @JsonProperty("slug")
    public String slug;
}
