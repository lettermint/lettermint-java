package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsRequest {
    @JsonProperty("metrics")
    public List<String> metrics;

    @JsonProperty("from")
    public String fromValue;

    @JsonProperty("to")
    public String to;

    @JsonProperty("timezone")
    public String timezone;

    @JsonProperty("include")
    public List<String> include;

    @JsonProperty("group_by")
    public List<String> groupBy;

    @JsonProperty("filters")
    public List<AnalyticsRequestFiltersItem> filters;

    @JsonProperty("interval")
    public String interval;

    @JsonProperty("compare")
    public String compare;

    @JsonProperty("include_trend")
    public Boolean includeTrend;

    @JsonProperty("sort")
    public AnalyticsRequestSort sort;

    @JsonProperty("limit")
    public Integer limit;

    @JsonProperty("cursor")
    public String cursor;
}
