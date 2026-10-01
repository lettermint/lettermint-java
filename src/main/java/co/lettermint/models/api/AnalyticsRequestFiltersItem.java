package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsRequestFiltersItem {
    @JsonProperty("dimension")
    public String dimension;

    @JsonProperty("operator")
    public String operator;

    @JsonProperty("values")
    public List<String> values;
}
