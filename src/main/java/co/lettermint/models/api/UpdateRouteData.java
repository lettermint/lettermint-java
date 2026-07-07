package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateRouteData {
    @JsonProperty("name")
    public String name;

    @JsonProperty("settings")
    public UpdateRouteSettingsData settings;

    @JsonProperty("inbound_settings")
    public UpdateRouteInboundSettingsData inboundSettings;
}
