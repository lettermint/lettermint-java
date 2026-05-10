package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateRouteData {
    @JsonProperty("name")
    public String name;

    @JsonProperty("settings")
    public Map<String, Object> settings;

    @JsonProperty("inbound_settings")
    public Map<String, Object> inboundSettings;
}
