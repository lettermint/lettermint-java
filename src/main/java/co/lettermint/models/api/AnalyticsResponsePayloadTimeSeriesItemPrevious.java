package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayloadTimeSeriesItemPrevious {
    @JsonProperty("metrics")
    public AnalyticsResponsePayloadTimeSeriesItemPreviousMetrics metrics;

    @JsonProperty("rate_bases")
    public AnalyticsResponsePayloadTimeSeriesItemPreviousRateBases rateBases;
}
