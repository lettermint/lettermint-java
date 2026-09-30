package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadSummaryPrevious {
    @JsonProperty("metrics")
    public AnalyticsResponsePayloadSummaryPreviousMetrics metrics;

    @JsonProperty("rate_bases")
    public AnalyticsResponsePayloadSummaryPreviousRateBases rateBases;
}
