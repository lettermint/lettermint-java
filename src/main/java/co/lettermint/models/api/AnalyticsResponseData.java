package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponseData {
    @JsonProperty("data")
    public Map<String, Object> data;

    @JsonProperty("meta")
    public Map<String, Object> meta;

    @JsonProperty("pagination")
    public List<String> pagination;
}
