package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamAddonData {
    @JsonProperty("type")
    public String type;

    @JsonProperty("expires_at")
    public String expiresAt;
}
