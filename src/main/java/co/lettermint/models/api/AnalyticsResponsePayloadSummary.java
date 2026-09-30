package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadSummary {
    @JsonProperty("metrics")
    public AnalyticsResponsePayloadSummaryMetrics metrics;

    @JsonProperty("rate_bases")
    public AnalyticsResponsePayloadSummaryRateBases rateBases;

    @JsonProperty("previous")
    public AnalyticsResponsePayloadSummaryPrevious previous;

    @JsonProperty("change")
    public Map<String, Map<String, Object>> change;
}
