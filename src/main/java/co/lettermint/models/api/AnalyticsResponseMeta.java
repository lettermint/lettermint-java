package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponseMeta {
    @JsonProperty("time_basis")
    public String timeBasis;

    @JsonProperty("timezone")
    public String timezone;

    @JsonProperty("interval")
    public String interval;

    @JsonProperty("from")
    public String fromValue;

    @JsonProperty("to")
    public String to;

    @JsonProperty("effective_to")
    public String effectiveTo;

    @JsonProperty("alignment")
    public String alignment;

    @JsonProperty("generated_at")
    public String generatedAt;

    @JsonProperty("available_since")
    public String availableSince;

    @JsonProperty("partial")
    public Boolean partial;

    @JsonProperty("ongoing")
    public Boolean ongoing;

    @JsonProperty("collection_completeness")
    public String collectionCompleteness;

    @JsonProperty("last_ingested_at")
    public String lastIngestedAt;

    @JsonProperty("metric_definition_version")
    public String metricDefinitionVersion;

    @JsonProperty("ranked_group_limit")
    public Integer rankedGroupLimit;

    @JsonProperty("comparison")
    public AnalyticsResponseMetaComparison comparison;
}
