package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePayload {
    @JsonProperty("summary")
    public AnalyticsResponsePayloadSummary summary;

    @JsonProperty("time_series")
    public List<AnalyticsResponsePayloadTimeSeriesItem> timeSeries;

    @JsonProperty("breakdown")
    public List<AnalyticsResponsePayloadBreakdownItem> breakdown;
}
