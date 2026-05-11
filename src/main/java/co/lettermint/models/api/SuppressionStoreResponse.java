package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SuppressionStoreResponse {
    @JsonProperty("message")
    public Object message;

    @JsonProperty("data")
    public Map<String, Object> data;
}
