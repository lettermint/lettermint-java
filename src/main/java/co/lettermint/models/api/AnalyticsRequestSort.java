package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsRequestSort {
    @JsonProperty("metric")
    public String metric;

    @JsonProperty("direction")
    public String direction;
}
