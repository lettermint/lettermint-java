package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadBreakdownItemPrevious {
    @JsonProperty("metrics")
    public AnalyticsResponsePayloadBreakdownItemPreviousMetrics metrics;

    @JsonProperty("rate_bases")
    public AnalyticsResponsePayloadBreakdownItemPreviousRateBases rateBases;
}
