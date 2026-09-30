package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadBreakdownItemTrendItemPrevious {
    @JsonProperty("metrics")
    public AnalyticsResponsePayloadBreakdownItemTrendItemPreviousMetrics metrics;

    @JsonProperty("rate_bases")
    public AnalyticsResponsePayloadBreakdownItemTrendItemPreviousRateBases rateBases;
}
