package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadBreakdownItem {
    @JsonProperty("metrics")
    public AnalyticsResponsePayloadBreakdownItemMetrics metrics;

    @JsonProperty("rate_bases")
    public AnalyticsResponsePayloadBreakdownItemRateBases rateBases;

    @JsonProperty("previous")
    public AnalyticsResponsePayloadBreakdownItemPrevious previous;

    @JsonProperty("change")
    public Map<String, Map<String, Object>> change;

    @JsonProperty("dimensions")
    public Map<String, String> dimensions;

    @JsonProperty("trend")
    public List<AnalyticsResponsePayloadBreakdownItemTrendItem> trend;
}
