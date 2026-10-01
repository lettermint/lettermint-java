package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreRouteData {
    @JsonProperty("name")
    public String name;

    @JsonProperty("route_type")
    public String routeType;

    @JsonProperty("slug")
    public String slug;

    @JsonProperty("settings")
    public UpdateRouteSettingsData settings;

    @JsonProperty("inbound_settings")
    public UpdateRouteInboundSettingsData inboundSettings;

    @JsonProperty("inbound_domain")
    public String inboundDomain;

    @JsonProperty("inbound_spam_threshold")
    public Double inboundSpamThreshold;

    @JsonProperty("attachment_delivery")
    public String attachmentDelivery;
}
