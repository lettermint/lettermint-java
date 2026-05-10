package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamUsageDetailData {
    @JsonProperty("current_period")
    public TeamUsagePeriodData currentPeriod;

    @JsonProperty("historical_usage")
    public List<TeamUsagePeriodData> historicalUsage;
}
