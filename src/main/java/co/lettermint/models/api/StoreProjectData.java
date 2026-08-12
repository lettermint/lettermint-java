package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreProjectData {
    @JsonProperty("name")
    public String name;

    @JsonProperty("smtp_enabled")
    public Boolean smtpEnabled;

    @JsonProperty("initial_routes")
    public String initialRoutes;

    @JsonProperty("short_token")
    public Boolean shortToken;
}
