package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageRecipientData {
    @JsonProperty("email")
    public String email;

    @JsonProperty("name")
    public String name;
}
