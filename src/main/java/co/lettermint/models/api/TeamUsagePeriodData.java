package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamUsagePeriodData {
    @JsonProperty("usage")
    public Integer usage;

    @JsonProperty("last_incremented_at")
    public String lastIncrementedAt;

    @JsonProperty("period_start")
    public String periodStart;

    @JsonProperty("period_end")
    public String periodEnd;
}
