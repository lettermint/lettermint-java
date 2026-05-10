package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatsData {
    @JsonProperty("from")
    public String fromValue;

    @JsonProperty("to")
    public String to;

    @JsonProperty("totals")
    public StatsTotalsData totals;

    @JsonProperty("daily")
    public List<StatsDailyData> daily;
}
