package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsResponsePagination {
    @JsonProperty("total_groups")
    public Integer totalGroups;

    @JsonProperty("returned_groups")
    public Integer returnedGroups;

    @JsonProperty("next_cursor")
    public String nextCursor;

    @JsonProperty("truncated")
    public Boolean truncated;
}
