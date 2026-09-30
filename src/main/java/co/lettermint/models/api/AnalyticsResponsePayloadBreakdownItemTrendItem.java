package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadBreakdownItemTrendItem {
    @JsonProperty("metrics")
    public AnalyticsResponsePayloadBreakdownItemTrendItemMetrics metrics;

    @JsonProperty("rate_bases")
    public AnalyticsResponsePayloadBreakdownItemTrendItemRateBases rateBases;

    @JsonProperty("previous")
    public AnalyticsResponsePayloadBreakdownItemTrendItemPrevious previous;

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
