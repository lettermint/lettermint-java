package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteIndexResponse {
    @JsonProperty("data")
    public List<RouteListData> data;

    @JsonProperty("path")
    public String path;

    @JsonProperty("per_page")
    public Integer perPage;

    @JsonProperty("next_cursor")
    public String nextCursor;

    @JsonProperty("next_page_url")
    public String nextPageUrl;

    @JsonProperty("prev_cursor")
    public String prevCursor;

    @JsonProperty("prev_page_url")
    public String prevPageUrl;
}
