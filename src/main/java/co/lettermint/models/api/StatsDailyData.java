package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatsDailyData {
    @JsonProperty("date")
    public String date;

    @JsonProperty("sent")
    public Integer sent;

    @JsonProperty("delivered")
    public Integer delivered;

    @JsonProperty("hard_bounced")
    public Integer hardBounced;

    @JsonProperty("spam_complaints")
    public Integer spamComplaints;

    @JsonProperty("opened")
    public Integer opened;

    @JsonProperty("clicked")
    public Integer clicked;

    @JsonProperty("inbound")
    public StatsInboundData inbound;

    @JsonProperty("transactional")
    public StatsTypeData transactional;

    @JsonProperty("broadcast")
    public StatsTypeData broadcast;

    @JsonProperty("observed_opened")
    public Integer observedOpened;

    @JsonProperty("human_opened")
    public Integer humanOpened;

    @JsonProperty("privacy_opened")
    public Integer privacyOpened;

    @JsonProperty("effective_opened")
    public Integer effectiveOpened;

    @JsonProperty("machine_opened")
    public Integer machineOpened;

    @JsonProperty("machine_clicked")
    public Integer machineClicked;
}
