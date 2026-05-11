package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatsTypeData {
    @JsonProperty("sent")
    public Integer sent;

    @JsonProperty("hard_bounced")
    public Integer hardBounced;

    @JsonProperty("spam_complaints")
    public Integer spamComplaints;
}
