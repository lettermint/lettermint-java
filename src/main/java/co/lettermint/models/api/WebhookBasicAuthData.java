package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookBasicAuthData {
    @JsonProperty("username")
    public String username;

    @JsonProperty("password")
    public String password;
}
