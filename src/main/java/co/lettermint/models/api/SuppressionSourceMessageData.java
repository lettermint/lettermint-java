package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SuppressionSourceMessageData {
    @JsonProperty("id")
    public String id;

    @JsonProperty("available")
    public Boolean available;

    @JsonProperty("subject")
    public String subject;

    @JsonProperty("created_at")
    public String createdAt;
}
