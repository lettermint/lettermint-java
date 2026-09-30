package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponse {
    @JsonProperty("data")
    public AnalyticsResponsePayload data;

    @JsonProperty("meta")
    public AnalyticsResponseMeta meta;

    @JsonProperty("pagination")
    public AnalyticsResponsePagination pagination;
}
