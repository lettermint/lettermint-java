package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadTimeSeriesItem {
    @JsonProperty("metrics")
    public AnalyticsResponsePayloadTimeSeriesItemMetrics metrics;

    @JsonProperty("rate_bases")
    public AnalyticsResponsePayloadTimeSeriesItemRateBases rateBases;

    @JsonProperty("previous")
    public AnalyticsResponsePayloadTimeSeriesItemPrevious previous;

    @JsonProperty("change")
    public Map<String, Map<String, Object>> change;

    @JsonProperty("from")
    public String fromValue;

    @JsonProperty("to")
    public String to;

    @JsonProperty("available")
    public Boolean available;

    @JsonProperty("partial")
    public Boolean partial;
}
